CREATE TABLE IF NOT EXISTS `article_favorite` (
    `id` BIGINT NOT NULL COMMENT '雪花ID',
    `user_id` BIGINT NOT NULL COMMENT '收藏用户ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `title_snapshot` VARCHAR(200) NOT NULL COMMENT '收藏时公开标题快照',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_favorite_user_article` (`user_id`, `article_id`),
    KEY `idx_article_favorite_user_deleted_created_at` (`user_id`, `deleted`, `created_at`),
    KEY `idx_article_favorite_article_deleted` (`article_id`, `deleted`),
    CONSTRAINT `fk_article_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_article_favorite_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章收藏关系表';
