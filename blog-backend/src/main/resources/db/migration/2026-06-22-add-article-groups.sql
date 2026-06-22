-- ============================================================
-- 增量升级脚本：新增“我的文章分组”功能
--
-- 使用场景：
-- 已经有数据的 blog_db 不能重新执行 init.sql，否则会删除并重建数据库。
-- 请在现有 blog_db 上执行本脚本，只会新增分组相关表，不会删除已有数据。
--
-- 执行前建议先备份数据库。
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `blog_db`;

-- ============================================================
-- 文章分组表
-- 分组只用于登录用户管理自己的文章，不影响公开标签和公开文章列表。
-- created_by 用于分组归属判断。
-- ============================================================
CREATE TABLE IF NOT EXISTS `article_group` (
    `id` BIGINT NOT NULL COMMENT '雪花ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分组名称',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '分组所属用户',
    `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_group_owner_name_deleted` (`created_by`, `name`, `deleted`),
    KEY `idx_article_group_owner_created_at` (`created_by`, `created_at`),
    KEY `idx_article_group_deleted_owner` (`deleted`, `created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分组表';

-- ============================================================
-- 文章分组关联表
-- 预留一篇文章属于多个分组的能力；当前前端先按单选分组使用。
-- 删除分组时只删除关联关系，不删除文章。
-- ============================================================
CREATE TABLE IF NOT EXISTS `article_group_relation` (
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

SET FOREIGN_KEY_CHECKS = 1;
