package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String email;
    private String role;
    private String status;
}
