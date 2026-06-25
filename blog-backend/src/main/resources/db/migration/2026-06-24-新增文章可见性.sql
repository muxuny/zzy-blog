-- ============================================================
-- 增量升级脚本：新增文章可见性
--
-- 使用场景：
-- 已经有数据的 blog_db 不能重新执行 init.sql，本脚本只为 article 表新增 visibility 字段。
-- public：公开可见；private：仅作者自己可见。
-- 历史文章默认 public，避免已有已发布文章被意外隐藏。
-- ============================================================

SET NAMES utf8mb4;

USE `blog_db`;

ALTER TABLE `article`
    ADD COLUMN `visibility` VARCHAR(20) NOT NULL DEFAULT 'public' COMMENT '可见性：public/private'
    AFTER `status`;

CREATE INDEX `idx_article_visibility` ON `article` (`visibility`);
