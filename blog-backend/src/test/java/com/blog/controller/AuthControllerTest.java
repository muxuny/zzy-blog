package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
import com.blog.entity.User;
import com.blog.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_shouldReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("token", "test-token");
        User mockUser = new User();
        mockUser.setUsername("admin");
        mockResult.put("user", mockUser);
        when(userService.login(any())).thenReturn(mockResult);

        Result<Map<String, Object>> result = authController.login(request);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData().get("token"));
        assertEquals("admin", ((User) result.getData().get("user")).getUsername());
    }
}
