package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 博客用户，角色和状态共同决定后台与创作中心权限。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    private String username;

    private String password;

    private String nickname;

    private String avatar;

    private String email;

    /** 用户角色，例如普通作者或管理员。 */
    private String role;

    /** 用户状态，注册审核和禁用逻辑会读取该字段。 */
    private String status;
}
