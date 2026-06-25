package com.blog.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static String requireMatch(Pattern pattern, String content, String label) {
        Matcher matcher = pattern.matcher(content);
        assertTrue(matcher.find(), "Expected to find " + label);
        return matcher.group(1);
    }

    private static String readString(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
