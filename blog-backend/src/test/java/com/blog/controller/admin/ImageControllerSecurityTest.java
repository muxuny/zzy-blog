package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.BusinessException;
import com.blog.service.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ImageControllerSecurityTest {

    @Test
    void classDoesNotGrantUserAccessToEveryImageEndpoint() {
        assertNull(ImageController.class.getAnnotation(PreAuthorize.class));
    }

    @Test
    void uploadAllowsUsersAndAdmins() throws NoSuchMethodException {
        Method upload = ImageController.class.getDeclaredMethod(
                "upload",
                org.springframework.web.multipart.MultipartFile.class,
                java.security.Principal.class);

        assertEquals("hasAnyRole('USER', 'ADMIN')", requirePreAuthorize(upload).value());
    }

    @Test
    void deleteAllowsOnlyAdmins() throws NoSuchMethodException {
        Method delete = ImageController.class.getDeclaredMethod("delete", Long.class);

        assertEquals("hasRole('ADMIN')", requirePreAuthorize(delete).value());
    }

    @Test
    void listAllowsOnlyAdmins() throws NoSuchMethodException {
        Method list = ImageController.class.getDeclaredMethod("list", long.class, long.class);

        assertEquals("hasRole('ADMIN')", requirePreAuthorize(list).value());
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void listRejectsOversizedPageBeforeQuerying() {
        ImageService imageService = mock(ImageService.class);
        ImageController controller = new ImageController(imageService);

        assertThatThrownBy(() -> controller.list(1, 101))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分页参数不合法");

        verify(imageService, never()).page(any(Page.class), any(Wrapper.class));
    }

    private static PreAuthorize requirePreAuthorize(Method method) {
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, method.getName() + " should declare method-level @PreAuthorize");
        return annotation;
    }
}
