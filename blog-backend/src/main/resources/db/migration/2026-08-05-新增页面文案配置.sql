CREATE TABLE IF NOT EXISTS `page_copy` (
    `id` BIGINT NOT NULL COMMENT '雪花ID',
    `copy_key` VARCHAR(80) NOT NULL COMMENT '页面文案标识',
    `page_name` VARCHAR(80) NOT NULL COMMENT '页面名称',
    `page_group` VARCHAR(50) NOT NULL COMMENT '页面分区',
    `eyebrow` VARCHAR(80) NOT NULL DEFAULT '' COMMENT '页面眉标',
    `title` VARCHAR(160) NOT NULL COMMENT '页面主标题',
    `description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '页面描述',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_page_copy_key` (`copy_key`),
    KEY `idx_page_copy_deleted_sort` (`deleted`, `sort_order`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='页面文案配置表';
