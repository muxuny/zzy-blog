package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
import com.blog.entity.User;
import java.util.Map;

/**
 * 用户登录、注册和后台审核服务。
 */
public interface UserService extends IService<User> {
    Map<String, Object> login(LoginRequest request);

    void register(RegisterRequest request);

    User getCurrentUser(String username);

    void approveUser(Long userId);

    void disableUser(Long userId);
}
