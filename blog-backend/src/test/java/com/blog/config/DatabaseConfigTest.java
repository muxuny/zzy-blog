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
        String normalized = migration.toLowerCase(Locale.ROOT);

        assertTrue(normalized.contains("create table if not exists `article_favorite`"));
        for (String column : Arrays.asList(
                "`id`", "`user_id`", "`article_id`", "`title_snapshot`",
                "`created_by`", "`created_at`", "`updated_by`", "`updated_at`",
                "`deleted`", "`version`")) {
            assertTrue(normalized.contains(column), "Missing favorite column " + column);
        }
        assertTrue(normalized.contains("unique key `uk_article_favorite_user_article` (`user_id`, `article_id`)"));
        assertTrue(normalized.contains("key `idx_article_favorite_user_deleted_created_at`"));
        assertTrue(normalized.contains("key `idx_article_favorite_article_deleted`"));
        assertTrue(!normalized.contains("drop table"));
        assertTrue(!normalized.contains("delete from"));
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
