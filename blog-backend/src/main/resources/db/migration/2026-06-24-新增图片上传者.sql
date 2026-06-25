-- ============================================================
-- 增量升级脚本：记录图片上传者
-- ============================================================
-- 已有数据保留为 NULL，表示历史上传者未知。
-- 新上传图片会由后端写入当前登录用户名。

ALTER TABLE `image`
    ADD COLUMN `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人' AFTER `url`;

CREATE INDEX `idx_image_created_by` ON `image` (`created_by`);
