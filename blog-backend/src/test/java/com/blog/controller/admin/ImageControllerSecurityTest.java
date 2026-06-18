package com.blog.controller.admin;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    private static PreAuthorize requirePreAuthorize(Method method) {
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, method.getName() + " should declare method-level @PreAuthorize");
        return annotation;
    }
}
