package com.blog.service;

import com.blog.common.BusinessException;
import com.blog.dto.RegisterRequest;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        // Create service with real PasswordEncoder, mock JwtUtil (null is fine for register tests)
        userService = new UserServiceImpl(new BCryptPasswordEncoder(), null) {
            @Override
            public User getOne(com.baomidou.mybatisplus.core.conditions.Wrapper<User> queryWrapper) {
                return userMapper.selectOne(queryWrapper);
            }
        };
    }

    @Test
    void register_shouldThrowWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("123456");

        when(userMapper.selectOne(any())).thenReturn(new User());

        assertThrows(BusinessException.class, () -> userService.register(request));
    }

    @Test
    void disableUser_shouldRejectAdminUser() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole("admin");
        admin.setStatus("active");

        userService = new UserServiceImpl(new BCryptPasswordEncoder(), null) {
            @Override
            public User getById(java.io.Serializable id) {
                return admin;
            }

            @Override
            public boolean updateById(User entity) {
                return userMapper.updateById(entity) > 0;
            }
        };

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.disableUser(1L));

        assertEquals("管理员账号不能被禁用", exception.getMessage());
        assertEquals("active", admin.getStatus());
        verify(userMapper, never()).updateById(any());
    }
}
