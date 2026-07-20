package com.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.ReadingHistoryItem;
import com.blog.dto.ReadingHistoryPageQuery;
import com.blog.dto.ReadingOverview;
import com.blog.service.ReadingHistoryService;
import com.blog.service.ReadingSpaceService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ReadingControllerTest {

    private final ReadingSpaceService readingSpaceService = mock(ReadingSpaceService.class);
    private final ReadingHistoryService readingHistoryService = mock(ReadingHistoryService.class);
    private final ReadingController controller = new ReadingController(readingSpaceService, readingHistoryService);
    private final Principal principal = () -> "alice";

    @Test
    void overview_shouldUseCurrentPrincipalAndReturnSuccess() {
        ReadingOverview overview = new ReadingOverview();
        when(readingSpaceService.getOverview("alice")).thenReturn(overview);

        Result<ReadingOverview> result = controller.overview(principal);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isSameAs(overview);
        verify(readingSpaceService).getOverview("alice");
        verifyNoMoreInteractions(readingSpaceService, readingHistoryService);
    }

    @Test
    void history_shouldUseCurrentPrincipalAndPreservePageResult() {
        ReadingHistoryPageQuery query = new ReadingHistoryPageQuery();
        query.setPage(2);
        query.setSize(7);
        ReadingHistoryItem item = new ReadingHistoryItem();
        Page<ReadingHistoryItem> history = new Page<>(2, 7);
        history.setTotal(15);
        history.setRecords(Collections.singletonList(item));
        when(readingHistoryService.getHistory(query, "alice")).thenReturn(history);

        PageResult<ReadingHistoryItem> result = controller.history(query, principal);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isSameAs(history.getRecords());
        assertThat(result.getTotal()).isEqualTo(15);
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(7);
        verify(readingHistoryService).getHistory(query, "alice");
        verifyNoMoreInteractions(readingSpaceService, readingHistoryService);
    }

    @Test
    void deleteHistory_shouldUseCurrentPrincipalAndReturnEmptySuccess() {
        Result<Void> result = controller.deleteHistory(42L, principal);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNull();
        verify(readingHistoryService).deleteHistory(42L, "alice");
        verifyNoMoreInteractions(readingSpaceService, readingHistoryService);
    }

    @Test
    void clearHistory_shouldUseCurrentPrincipalAndReturnEmptySuccess() {
        Result<Void> result = controller.clearHistory(principal);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNull();
        verify(readingHistoryService).clearHistory("alice");
        verifyNoMoreInteractions(readingSpaceService, readingHistoryService);
    }

    @Test
    void mappings_andAuthorization_shouldBeExactAndUnambiguous() throws NoSuchMethodException {
        RequestMapping requestMapping = ReadingController.class.getAnnotation(RequestMapping.class);
        PreAuthorize preAuthorize = ReadingController.class.getAnnotation(PreAuthorize.class);

        assertThat(requestMapping).isNotNull();
        assertThat(requestMapping.value()).containsExactly("/api/my/reading");
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAnyRole('USER', 'ADMIN')");
        assertMapping("overview", new Class[]{Principal.class}, GetMapping.class, "/overview");
        assertMapping("history", new Class[]{ReadingHistoryPageQuery.class, Principal.class},
                GetMapping.class, "/history");
        assertMapping("deleteHistory", new Class[]{Long.class, Principal.class},
                DeleteMapping.class, "/history/{articleId}");
        assertMapping("clearHistory", new Class[]{Principal.class}, DeleteMapping.class, "/history");
    }

    private static void assertMapping(String methodName, Class<?>[] parameterTypes,
                                      Class<? extends java.lang.annotation.Annotation> mappingType,
                                      String path) throws NoSuchMethodException {
        Method method = ReadingController.class.getMethod(methodName, parameterTypes);
        assertThat(method.getAnnotationsByType(GetMapping.class)).hasSize(mappingType == GetMapping.class ? 1 : 0);
        assertThat(method.getAnnotationsByType(DeleteMapping.class)).hasSize(mappingType == DeleteMapping.class ? 1 : 0);
        if (mappingType == GetMapping.class) {
            assertThat(method.getAnnotation(GetMapping.class).value()).containsExactly(path);
        } else {
            assertThat(method.getAnnotation(DeleteMapping.class).value()).containsExactly(path);
        }
    }
}
