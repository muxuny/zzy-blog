package com.blog.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static String requireMatch(Pattern pattern, String content, String label) {
        Matcher matcher = pattern.matcher(content);
        assertTrue(matcher.find(), "Expected to find " + label);
        return matcher.group(1);
    }

    private static String readString(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
