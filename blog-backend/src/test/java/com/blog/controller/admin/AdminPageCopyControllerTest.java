package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.dto.PageCopyItem;
import com.blog.dto.PageCopyUpdateRequest;
import com.blog.service.PageCopyService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminPageCopyControllerTest {

    private final PageCopyService pageCopyService = mock(PageCopyService.class);

    private final AdminPageCopyController controller = new AdminPageCopyController(pageCopyService);

    @Test
    void classAllowsOnlyAdmins() {
        PreAuthorize annotation = AdminPageCopyController.class.getAnnotation(PreAuthorize.class);

        assertEquals("hasRole('ADMIN')", annotation.value());
    }

    @Test
    void listDelegatesToService() {
        PageCopyItem item = new PageCopyItem();
        item.setCopyKey("admin.dashboard");
        when(pageCopyService.listAll()).thenReturn(Collections.singletonList(item));

        Result<List<PageCopyItem>> result = controller.list();

        assertThat(result.getData()).extracting(PageCopyItem::getCopyKey).containsExactly("admin.dashboard");
        verify(pageCopyService).listAll();
    }

    @Test
    void updateDelegatesToService() {
        PageCopyUpdateRequest request = new PageCopyUpdateRequest();
        request.setTitle("新首页");
        PageCopyItem updated = new PageCopyItem();
        updated.setCopyKey("home.hero");
        updated.setTitle("新首页");
        when(pageCopyService.updateCopy("home.hero", request)).thenReturn(updated);

        Result<PageCopyItem> result = controller.update("home.hero", request);

        assertThat(result.getData().getTitle()).isEqualTo("新首页");
        verify(pageCopyService).updateCopy("home.hero", request);
    }

    @Test
    void resetDelegatesToService() {
        PageCopyItem item = new PageCopyItem();
        item.setCopyKey("home.hero");
        when(pageCopyService.resetAll()).thenReturn(Collections.singletonList(item));

        Result<List<PageCopyItem>> result = controller.reset();

        assertThat(result.getData()).extracting(PageCopyItem::getCopyKey).containsExactly("home.hero");
        verify(pageCopyService).resetAll();
    }
}
