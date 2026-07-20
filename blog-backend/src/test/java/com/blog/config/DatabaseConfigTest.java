package com.blog.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseConfigTest {

    @Test
    void datasourceDatabaseMatchesInitScriptDatabase() throws IOException {
        String applicationYml = readString(Paths.get("src/main/resources/application.yml"));
        String initSql = readString(Paths.get("src/main/resources/db/init.sql"));

        String datasourceDatabase = requireMatch(
                Pattern.compile("jdbc:mysql://[^/]+/([^?\\s]+)"),
                applicationYml,
                "spring datasource JDBC database name");
        String createdDatabase = requireMatch(
                Pattern.compile("CREATE\\s+DATABASE\\s+`?([\\w-]+)`?", Pattern.CASE_INSENSITIVE),
                initSql,
                "init.sql CREATE DATABASE name");
        String usedDatabase = requireMatch(
                Pattern.compile("USE\\s+`?([\\w-]+)`?", Pattern.CASE_INSENSITIVE),
                initSql,
                "init.sql USE database name");

        assertEquals("blog_db", datasourceDatabase);
        assertEquals("blog_db", createdDatabase);
        assertEquals("blog_db", usedDatabase);
        assertEquals(createdDatabase, datasourceDatabase);
        assertEquals(usedDatabase, datasourceDatabase);
    }

    @Test
    void localMysqlConnectionAllowsPublicKeyRetrievalWhenSslIsDisabled() throws IOException {
        String applicationYml = readString(Paths.get("src/main/resources/application.yml"));

        String datasourceUrl = requireMatch(
                Pattern.compile("url:\\s*(jdbc:mysql://\\S+)"),
                applicationYml,
                "spring datasource JDBC URL");

        assertTrue(datasourceUrl.contains("useSSL=false"),
                "Expected local datasource to disable SSL explicitly");
        assertTrue(datasourceUrl.contains("allowPublicKeyRetrieval=true"),
                "MySQL 8 caching_sha2_password needs allowPublicKeyRetrieval=true when SSL is disabled");
    }

    @Test
    void migrationScriptsMustBeIncrementalAndPreserveExistingData() throws IOException {
        String groupSql = readString(Paths.get("src/main/resources/db/migration/2026-06-22-add-article-groups.sql"));
        String groupNormalized = groupSql.toLowerCase(Locale.ROOT);
        assertTrue(groupNormalized.contains("create table if not exists `article_group`"),
                "Expected migration to create article_group without requiring database rebuild");
        assertTrue(groupNormalized.contains("create table if not exists `article_group_relation`"),
                "Expected migration to create article_group_relation without requiring database rebuild");

        String visibilitySql = readString(Paths.get("src/main/resources/db/migration/2026-06-24-新增文章可见性.sql"));
        String visibilityNormalized = visibilitySql.toLowerCase(Locale.ROOT);
        assertTrue(visibilityNormalized.contains("alter table `article`"),
                "Expected migration to alter the existing article table");
        assertTrue(visibilityNormalized.contains("add column `visibility`"),
                "Expected migration to add article visibility");
        assertTrue(visibilityNormalized.contains("default 'public'"),
                "Existing articles should remain public by default");

        String imageOwnerSql = readString(Paths.get("src/main/resources/db/migration/2026-06-24-新增图片上传者.sql"));
        String imageOwnerNormalized = imageOwnerSql.toLowerCase(Locale.ROOT);
        assertTrue(imageOwnerNormalized.contains("alter table `image`"),
                "Expected migration to alter the existing image table");
        assertTrue(imageOwnerNormalized.contains("add column `created_by`"),
                "Expected migration to add image uploader");
        assertTrue(imageOwnerNormalized.contains("default null"),
                "Existing images should keep unknown uploader as NULL");

        String normalized = groupNormalized + "\n" + visibilityNormalized + "\n" + imageOwnerNormalized;
        assertTrue(!normalized.contains("drop database"), "Migration must not drop databases");
        assertTrue(!normalized.contains("drop table"), "Migration must not drop tables");
        assertTrue(!normalized.contains("truncate table"), "Migration must not truncate tables");
        assertTrue(!normalized.contains("delete from `article`"), "Migration must not delete articles");
        assertTrue(!normalized.contains("delete from article"), "Migration must not delete articles");
    }

    @Test
    void favoriteSchemaIncludesBusinessAndCommonFields() throws IOException {
        String migration = readString(Paths.get(
                "src/main/resources/db/migration/2026-07-16-新增文章收藏.sql"));
        String initSql = readString(Paths.get("src/main/resources/db/init.sql"));
        String migrationDefinition = normalizeSql(extractFavoriteTableDefinition(migration, "migration"));
        String initDefinition = normalizeSql(extractFavoriteTableDefinition(initSql, "init.sql"));

        assertEquals(migrationDefinition, initDefinition,
                "init.sql and migration must use the same article_favorite table definition");
        for (String columnDefinition : Arrays.asList(
                "`id` bigint not null comment '雪花id',",
                "`user_id` bigint not null comment '收藏用户id',",
                "`article_id` bigint not null comment '文章id',",
                "`title_snapshot` varchar(200) not null comment '收藏时公开标题快照',",
                "`created_by` varchar(50) default null comment '创建人',",
                "`created_at` datetime default null comment '创建时间',",
                "`updated_by` varchar(50) default null comment '更新人',",
                "`updated_at` datetime default null comment '更新时间',",
                "`deleted` tinyint(1) not null default 0 comment '逻辑删除：0未删除，1已删除',",
                "`version` int not null default 0 comment '乐观锁版本号',")) {
            assertTrue(migrationDefinition.contains(columnDefinition),
                    "Missing favorite column definition " + columnDefinition);
        }
        assertTrue(migrationDefinition.contains("primary key (`id`)"));
        assertTrue(migrationDefinition.contains(
                "unique key `uk_article_favorite_user_article` (`user_id`, `article_id`)"));
        assertTrue(migrationDefinition.contains(
                "key `idx_article_favorite_user_deleted_created_at` (`user_id`, `deleted`, `created_at`)"));
        assertTrue(migrationDefinition.contains(
                "key `idx_article_favorite_article_deleted` (`article_id`, `deleted`)"));
        assertTrue(migrationDefinition.contains(
                "constraint `fk_article_favorite_user` foreign key (`user_id`) references `user` (`id`) "
                        + "on delete cascade on update cascade"));
        assertTrue(migrationDefinition.contains(
                "constraint `fk_article_favorite_article` foreign key (`article_id`) references `article` (`id`) "
                        + "on delete cascade on update cascade"));

        String normalizedMigration = normalizeSql(migration);
        assertTrue(!normalizedMigration.contains("drop table"));
        assertTrue(!normalizedMigration.contains("delete from"));
    }

    @Test
    void favoriteMapperStatementsMatchIdempotentCancelAndRestoreContract() throws IOException {
        String xml = readString(Paths.get("src/main/resources/mapper/ArticleFavoriteMapper.xml"));

        assertEquals(normalizeWhitespace(
                "INSERT INTO article_favorite "
                        + "(id, user_id, article_id, title_snapshot, created_by, created_at, "
                        + "updated_by, updated_at, deleted, version) "
                        + "VALUES (#{id}, #{userId}, #{articleId}, #{titleSnapshot}, #{username}, #{now}, "
                        + "#{username}, #{now}, 0, 0) "
                        + "ON DUPLICATE KEY UPDATE "
                        + "title_snapshot = IF(deleted = 1, VALUES(title_snapshot), title_snapshot), "
                        + "created_by = IF(deleted = 1, VALUES(created_by), created_by), "
                        + "created_at = IF(deleted = 1, VALUES(created_at), created_at), "
                        + "updated_by = IF(deleted = 1, VALUES(updated_by), updated_by), "
                        + "updated_at = IF(deleted = 1, VALUES(updated_at), updated_at), "
                        + "version = IF(deleted = 1, version + 1, version), "
                        + "deleted = 0"),
                extractMapperStatement(xml, "insert", "upsertFavorite"));
        assertEquals(normalizeWhitespace(
                "UPDATE article_favorite "
                        + "SET deleted = 1, updated_by = #{username}, updated_at = #{now}, "
                        + "version = version + 1 "
                        + "WHERE user_id = #{userId} AND article_id = #{articleId} AND deleted = 0"),
                extractMapperStatement(xml, "update", "cancelFavorite"));
        assertEquals(normalizeWhitespace(
                "SELECT COUNT(1) FROM article_favorite "
                        + "WHERE user_id = #{userId} AND article_id = #{articleId} AND deleted = 0"),
                extractMapperStatement(xml, "select", "countActiveFavorite"));
    }

    @Test
    void favoritePageMapperQueryPreservesVisibilityAndFilteringContract() throws IOException {
        String xml = readString(Paths.get("src/main/resources/mapper/ArticleFavoriteMapper.xml"));
        String query = extractMapperStatement(xml, "select", "selectFavoritePage");

        assertTrue(query.contains(normalizeWhitespace(
                "WHERE f.user_id = #{userId} AND f.deleted = 0")),
                "Favorite page must only include the current user's active favorites");
        assertTrue(query.contains(normalizeWhitespace(
                "a.id IS NOT NULL AND a.deleted = 0 AND a.status = 'published' "
                        + "AND (a.visibility = 'public' OR a.visibility IS NULL) "
                        + "AND (a.title LIKE CONCAT('%', #{keyword}, '%') "
                        + "OR a.summary LIKE CONCAT('%', #{keyword}, '%'))")),
                "Available favorites must search the current public title and summary");
        assertTrue(query.contains(normalizeWhitespace(
                "(a.id IS NULL OR a.deleted != 0 OR a.status != 'published' "
                        + "OR (a.visibility != 'public' AND a.visibility IS NOT NULL)) "
                        + "AND f.title_snapshot LIKE CONCAT('%', #{keyword}, '%')")),
                "Unavailable favorites must search only the saved title snapshot");
        assertTrue(query.contains(normalizeWhitespace(
                "SELECT 1 FROM article_tag at "
                        + "INNER JOIN `tag` t ON t.id = at.tag_id AND t.deleted = 0 "
                        + "WHERE at.article_id = f.article_id AND at.tag_id = #{tagId} "
                        + "AND at.deleted = 0")),
                "Tag filtering must require both an active tag and an active article-tag relation");
        assertTrue(query.contains("ORDER BY f.created_at DESC, f.id DESC"),
                "Favorite page ordering must be stable and newest-first");
    }

    @Test
    void readingHistorySchemaIncludesBusinessAndCommonFields() throws IOException {
        String migration = readString(Paths.get(
                "src/main/resources/db/migration/2026-07-20-新增文章阅读历史.sql"));
        String initSql = readString(Paths.get("src/main/resources/db/init.sql"));
        String migrationDefinition = normalizeSql(extractReadingHistoryTableDefinition(migration, "migration"));
        String initDefinition = normalizeSql(extractReadingHistoryTableDefinition(initSql, "init.sql"));

        assertEquals(migrationDefinition, initDefinition,
                "init.sql and migration must use the same article_reading_history table definition");
        for (String columnDefinition : Arrays.asList(
                "`id` bigint not null comment '雪花id',",
                "`user_id` bigint not null comment '阅读用户id',",
                "`article_id` bigint not null comment '文章id',",
                "`title_snapshot` varchar(200) not null comment '最近阅读时公开标题快照',",
                "`first_read_at` datetime not null comment '首次阅读时间',",
                "`last_read_at` datetime not null comment '最近阅读时间',",
                "`read_count` int not null default 1 comment '阅读次数',",
                "`created_by` varchar(50) default null comment '创建人',",
                "`created_at` datetime default null comment '创建时间',",
                "`updated_by` varchar(50) default null comment '更新人',",
                "`updated_at` datetime default null comment '更新时间',",
                "`deleted` tinyint(1) not null default 0 comment '逻辑删除：0未删除，1已删除',",
                "`version` int not null default 0 comment '乐观锁版本号',")) {
            assertTrue(migrationDefinition.contains(columnDefinition),
                    "Missing reading history column definition " + columnDefinition);
        }
        assertTrue(migrationDefinition.contains("primary key (`id`)"));
        assertTrue(migrationDefinition.contains(
                "unique key `uk_article_reading_history_user_article` (`user_id`, `article_id`)"));
        assertTrue(migrationDefinition.contains(
                "key `idx_article_reading_history_user_last` (`user_id`, `deleted`, `last_read_at`)"));
        assertTrue(migrationDefinition.contains(
                "key `idx_article_reading_history_article` (`article_id`, `deleted`)"));
        assertTrue(migrationDefinition.contains(
                "constraint `fk_article_reading_history_user` foreign key (`user_id`) references `user` (`id`) "
                        + "on delete cascade on update cascade"));
        assertTrue(migrationDefinition.contains(
                "constraint `fk_article_reading_history_article` foreign key (`article_id`) references `article` (`id`) "
                        + "on delete cascade on update cascade"));

        String normalizedMigration = normalizeSql(migration);
        assertTrue(!normalizedMigration.contains("drop "));
        assertTrue(!normalizedMigration.contains("delete from"));
    }

    @Test
    void readingHistoryMapperUsesAtomicRecordAndScopedDeleteContract() throws IOException {
        String xml = readString(Paths.get("src/main/resources/mapper/ArticleReadingHistoryMapper.xml"));
        assertEquals(normalizeWhitespace(
                "INSERT INTO article_reading_history "
                        + "(id, user_id, article_id, title_snapshot, first_read_at, last_read_at, read_count, "
                        + "created_by, created_at, updated_by, updated_at, deleted, version) "
                        + "VALUES (#{id}, #{userId}, #{articleId}, #{titleSnapshot}, #{now}, #{now}, 1, "
                        + "#{username}, #{now}, #{username}, #{now}, 0, 0) "
                        + "ON DUPLICATE KEY UPDATE "
                        + "title_snapshot = VALUES(title_snapshot), "
                        + "first_read_at = IF(deleted = 1, VALUES(first_read_at), first_read_at), "
                        + "last_read_at = VALUES(last_read_at), "
                        + "read_count = IF(deleted = 1, 1, read_count + 1), "
                        + "updated_by = VALUES(updated_by), "
                        + "updated_at = VALUES(updated_at), "
                        + "version = version + 1, "
                        + "deleted = 0"),
                extractMapperStatement(xml, "insert", "upsertHistory"));
        assertEquals(normalizeWhitespace(
                "UPDATE article_reading_history SET deleted = 1, updated_by = #{username}, "
                        + "updated_at = #{now}, version = version + 1 "
                        + "WHERE user_id = #{userId} AND article_id = #{articleId} AND deleted = 0"),
                extractMapperStatement(xml, "update", "deleteHistory"));
        assertEquals(normalizeWhitespace(
                "UPDATE article_reading_history SET deleted = 1, updated_by = #{username}, "
                        + "updated_at = #{now}, version = version + 1 "
                        + "WHERE user_id = #{userId} AND deleted = 0"),
                extractMapperStatement(xml, "update", "clearHistory"));
    }

    @Test
    void readingHistoryMapperQueriesPreserveVisibilityAndOrderingContract() throws IOException {
        String xml = readString(Paths.get("src/main/resources/mapper/ArticleReadingHistoryMapper.xml"));
        String pageQuery = extractMapperStatement(xml, "select", "selectHistoryPage");
        String lastAvailableQuery = extractMapperStatement(xml, "select", "selectLastAvailable");
        String visibility = normalizeWhitespace(
                "a.id IS NOT NULL AND a.deleted = 0 AND a.status = 'published' "
                        + "AND (a.visibility = 'public' OR a.visibility IS NULL)");

        assertTrue(pageQuery.contains(normalizeWhitespace(
                "WHERE h.user_id = #{userId} AND h.deleted = 0")),
                "History page must only include the current user's active history");
        assertTrue(pageQuery.contains(visibility),
                "History page availability must only recognize public published articles");
        assertTrue(pageQuery.contains("ORDER BY h.last_read_at DESC, h.id DESC"),
                "History page ordering must be stable and most-recent-first");
        assertTrue(lastAvailableQuery.contains(normalizeWhitespace(
                "FROM article_reading_history h INNER JOIN article a ON a.id = h.article_id")),
                "Last available history must join the current article");
        assertTrue(lastAvailableQuery.contains(normalizeWhitespace(
                "WHERE h.user_id = #{userId} AND h.deleted = 0 AND " + visibility)),
                "Last available history must use the same public visibility rule");
        assertTrue(lastAvailableQuery.contains("ORDER BY h.last_read_at DESC, h.id DESC"));
        assertTrue(lastAvailableQuery.contains("LIMIT 1"));
    }

    private static String extractMapperStatement(String xml, String element, String id) {
        String withoutComments = xml.replaceAll("(?s)<!--.*?-->", "");
        Pattern pattern = Pattern.compile(
                "<" + Pattern.quote(element) + "\\s+id\\s*=\\s*\"" + Pattern.quote(id)
                        + "\"[^>]*>(.*?)</" + Pattern.quote(element) + ">",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(withoutComments);
        assertTrue(matcher.find(), "Expected mapper statement " + id);
        String statement = matcher.group(1);
        assertTrue(!matcher.find(), "Expected exactly one mapper statement " + id);
        return normalizeWhitespace(statement);
    }

    private static String normalizeWhitespace(String content) {
        return content.trim().replaceAll("\\s+", " ");
    }

    private static String extractFavoriteTableDefinition(String sql, String source) {
        Pattern pattern = Pattern.compile(
                "CREATE\\s+TABLE\\s+IF\\s+NOT\\s+EXISTS\\s+`article_favorite`\\s*"
                        + "\\(.*?\\)\\s*ENGINE\\s*=\\s*InnoDB\\s+DEFAULT\\s+CHARSET\\s*=\\s*utf8mb4\\s+"
                        + "COLLATE\\s*=\\s*utf8mb4_unicode_ci\\s+COMMENT\\s*=\\s*'文章收藏关系表'\\s*;",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sql);
        assertTrue(matcher.find(),
                "Expected to find complete article_favorite CREATE TABLE block in " + source);
        return matcher.group();
    }

    private static String extractReadingHistoryTableDefinition(String sql, String source) {
        Pattern pattern = Pattern.compile(
                "CREATE\\s+TABLE\\s+IF\\s+NOT\\s+EXISTS\\s+`article_reading_history`\\s*"
                        + "\\(.*?\\)\\s*ENGINE\\s*=\\s*InnoDB\\s+DEFAULT\\s+CHARSET\\s*=\\s*utf8mb4\\s+"
                        + "COLLATE\\s*=\\s*utf8mb4_unicode_ci\\s+COMMENT\\s*=\\s*'文章阅读历史表'\\s*;",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sql);
        assertTrue(matcher.find(),
                "Expected to find complete article_reading_history CREATE TABLE block in " + source);
        return matcher.group();
    }

    private static String normalizeSql(String sql) {
        return sql.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static String requireMatch(Pattern pattern, String content, String label) {
        Matcher matcher = pattern.matcher(content);
        assertTrue(matcher.find(), "Expected to find " + label);
        return matcher.group(1);
    }

    private static String readString(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
