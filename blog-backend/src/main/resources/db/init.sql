-- ============================================================
-- 博客系统数据库初始化脚本（当前项目版）
--
-- 当前后端代码使用以下 7 张表：
-- user、article、article_group、tag、article_tag、article_group_relation、image
--
-- 历史表 comment、category 当前没有后端实体、Mapper、接口或前端入口，
-- 本脚本不再创建，后续开发评论/分类功能时再新增。
--
-- 注意：执行本脚本会删除并重建 blog_db，请先备份旧数据。
-- 超级管理员账号：admin
-- 超级管理员临时密码：Admin@123456
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS `blog_db`;
CREATE DATABASE `blog_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `blog_db`;

-- ============================================================
-- 用户表
-- role：当前代码使用 admin/user，小写；JWT 过滤器会自动转换成 ROLE_ADMIN/ROLE_USER。
-- status：当前代码使用 pending/active/disabled。
-- ============================================================
CREATE TABLE `user` (
    `id` BIGINT NOT NULL COMMENT '雪花ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT 'BCrypt密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色：admin/user',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/active/disabled',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    KEY `idx_user_status` (`status`),
    KEY `idx_user_role` (`role`),
    KEY `idx_user_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 文章表
-- status：当前代码使用 draft/pending/published/rejected。
-- created_by 会用于文章作者展示和普通用户删除权限判断。
-- ============================================================
CREATE TABLE `article` (
    `id` BIGINT NOT NULL COMMENT '雪花ID',
    `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
    `content` LONGTEXT DEFAULT NULL COMMENT '文章内容',
    `summary` VARCHAR(500) DEFAULT NULL COMMENT '文章摘要',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片URL',
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态：draft/pending/published/rejected',
    `visibility` VARCHAR(20) NOT NULL DEFAULT 'public' COMMENT '可见性：public/private',
    `review_reason` VARCHAR(500) DEFAULT NULL COMMENT '审核驳回原因',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_article_status` (`status`),
    KEY `idx_article_visibility` (`visibility`),
    KEY `idx_article_created_at` (`created_at`),
    KEY `idx_article_created_by` (`created_by`),
    KEY `idx_article_deleted_status_created_at` (`deleted`, `status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- ============================================================
-- 文章分组表
-- 分组只用于登录用户管理自己的文章，不影响公开标签和公开文章列表。
-- created_by 用于分组归属判断。
-- ============================================================
CREATE TABLE `article_group` (
    `id` BIGINT NOT NULL COMMENT '雪花ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分组名称',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '分组所属用户',
    `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_article_group_owner_created_at` (`created_by`, `created_at`),
    UNIQUE KEY `uk_article_group_owner_name_deleted` (`created_by`, `name`, `deleted`),
    KEY `idx_article_group_deleted_owner` (`deleted`, `created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分组表';

-- ============================================================
-- 标签表
-- ============================================================
CREATE TABLE `tag` (
    `id` BIGINT NOT NULL COMMENT '雪花ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_name` (`name`),
    KEY `idx_tag_deleted_name` (`deleted`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ============================================================
-- 文章标签关联表
-- ============================================================
CREATE TABLE `article_tag` (
    `id` BIGINT NOT NULL COMMENT '雪花ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_article_tag_article_id` (`article_id`),
    KEY `idx_article_tag_tag_id` (`tag_id`),
    CONSTRAINT `fk_article_tag_article`
        FOREIGN KEY (`article_id`) REFERENCES `article` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_article_tag_tag`
        FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

-- ============================================================
-- 文章分组关联表
-- 预留一篇文章属于多个分组的能力；当前前端先按单选分组使用。
-- 删除分组时只删除关联关系，不删除文章。
-- ============================================================
CREATE TABLE `article_group_relation` (
    `id` BIGINT NOT NULL COMMENT '雪花ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `group_id` BIGINT NOT NULL COMMENT '分组ID',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_article_group_relation_article_id` (`article_id`),
    KEY `idx_article_group_relation_group_id` (`group_id`),
    KEY `idx_article_group_relation_group_article` (`group_id`, `article_id`),
    CONSTRAINT `fk_article_group_relation_article`
        FOREIGN KEY (`article_id`) REFERENCES `article` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_article_group_relation_group`
        FOREIGN KEY (`group_id`) REFERENCES `article_group` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分组关联表';

-- ============================================================
-- 图片表
-- ============================================================
CREATE TABLE `image` (
    `id` BIGINT NOT NULL COMMENT '雪花ID',
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `filename` VARCHAR(255) NOT NULL COMMENT '存储文件名',
    `size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小，单位字节',
    `mime_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    `url` VARCHAR(255) NOT NULL COMMENT '访问URL',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_image_filename` (`filename`),
    KEY `idx_image_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 初始化数据
-- ============================================================

-- 超级管理员
-- 密码：Admin@123456
INSERT INTO `user` (
    `id`, `username`, `password`, `nickname`, `avatar`, `email`,
    `role`, `status`, `created_by`, `created_at`, `updated_by`, `updated_at`,
    `deleted`, `version`
) VALUES (
    758902345678901234,
    'admin',
    '$2a$10$Bj4QCdhv5SpBflaf50rQu.Fp3PJJb5aEKYtWphQFwlBniy5geQcgC',
    '超级管理员',
    NULL,
    'admin@example.com',
    'admin',
    'active',
    'system',
    NOW(),
    'system',
    NOW(),
    0,
    0
);

-- 基础标签
INSERT INTO `tag` (
    `id`, `name`, `created_by`, `created_at`, `updated_by`, `updated_at`, `deleted`, `version`
) VALUES
    (758902345678901301, '前端', 'system', NOW(), 'system', NOW(), 0, 0),
    (758902345678901302, '后端', 'system', NOW(), 'system', NOW(), 0, 0),
    (758902345678901303, '数据库', 'system', NOW(), 'system', NOW(), 0, 0),
    (758902345678901304, '工程化', 'system', NOW(), 'system', NOW(), 0, 0),
    (758902345678901305, '随笔', 'system', NOW(), 'system', NOW(), 0, 0);

-- 示例文章：用于重建后验证首页、文章详情和标签筛选，可在后台删除
INSERT INTO `article` (
    `id`, `title`, `content`, `summary`, `cover_image`, `status`, `visibility`, `review_reason`, `view_count`,
    `created_by`, `created_at`, `updated_by`, `updated_at`, `deleted`, `version`
) VALUES (
    758902345678901401,
    '欢迎使用博客系统',
    '这是数据库重建后的第一篇示例文章。你可以在管理后台编辑、删除，或创建新的文章内容。',
    '数据库重建完成后用于验证首页、文章详情和标签筛选的示例内容。',
    NULL,
    'published',
    'public',
    NULL,
    0,
    'admin',
    NOW(),
    'admin',
    NOW(),
    0,
    0
);

INSERT INTO `article_tag` (
    `id`, `article_id`, `tag_id`, `created_by`, `created_at`, `updated_by`, `updated_at`, `deleted`, `version`
) VALUES
    (758902345678901501, 758902345678901401, 758902345678901304, 'admin', NOW(), 'admin', NOW(), 0, 0),
    (758902345678901502, 758902345678901401, 758902345678901305, 'admin', NOW(), 'admin', NOW(), 0, 0);
