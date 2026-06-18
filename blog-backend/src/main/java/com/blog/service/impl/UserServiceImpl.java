package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.BusinessException;
import com.blog.config.JwtUtil;
import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Map<String, Object> login(LoginRequest request) {
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!"active".equals(user.getStatus())) {
            throw new BusinessException("账号未激活或被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    @Override
    public void register(RegisterRequest request) {
        User exist = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole("user");
        user.setStatus("pending");
        save(user);
    }

    @Override
    public User getCurrentUser(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public void approveUser(Long userId) {
        User user = getById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus("active");
        updateById(user);
    }

    @Override
    public void disableUser(Long userId) {
        User user = getById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus("disabled");
        updateById(user);
    }
}
