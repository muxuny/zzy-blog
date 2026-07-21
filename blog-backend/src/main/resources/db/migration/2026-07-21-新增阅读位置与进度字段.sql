ALTER TABLE `article_reading_history`
    ADD COLUMN `progress_percent` INT NOT NULL DEFAULT 0 COMMENT '阅读进度百分比' AFTER `read_count`,
    ADD COLUMN `scroll_y` INT NOT NULL DEFAULT 0 COMMENT '最近阅读滚动位置' AFTER `progress_percent`,
    ADD COLUMN `anchor_id` VARCHAR(160) DEFAULT NULL COMMENT '最近阅读标题锚点' AFTER `scroll_y`,
    ADD COLUMN `anchor_offset` INT DEFAULT NULL COMMENT '相对锚点偏移' AFTER `anchor_id`,
    ADD COLUMN `article_updated_at_snapshot` DATETIME DEFAULT NULL COMMENT '保存位置时文章更新时间快照' AFTER `anchor_offset`,
    ADD COLUMN `position_updated_at` DATETIME DEFAULT NULL COMMENT '最近保存阅读位置时间' AFTER `article_updated_at_snapshot`;
