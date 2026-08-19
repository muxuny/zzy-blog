-- 后台仪表盘全站聚合查询性能增强
-- 只新增索引，不修改表结构、不删除数据；由用户手动执行。
ALTER TABLE `article`
    ADD KEY `idx_article_deleted_status_visibility_view_count` (`deleted`, `status`, `visibility`, `view_count`);

ALTER TABLE `article_reading_history`
    ADD KEY `idx_reading_history_deleted_last_read_at` (`deleted`, `last_read_at`);

ALTER TABLE `article_favorite`
    ADD KEY `idx_article_favorite_deleted_created_at` (`deleted`, `created_at`);

ALTER TABLE `image`
    ADD KEY `idx_image_deleted_created_at` (`deleted`, `created_at`);
