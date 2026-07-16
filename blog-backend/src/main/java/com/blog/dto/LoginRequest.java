package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求参数。
 */
@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
