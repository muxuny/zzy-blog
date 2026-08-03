package com.blog.dto;

import com.blog.entity.Article;
import com.blog.entity.Tag;
import com.blog.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Full-site aggregate payload for the admin dashboard.
 */
@Data
public class AdminDashboardOverview {
    private Metrics metrics = new Metrics();

    private List<StatusCount> articleStatus = Collections.emptyList();

    private List<Article> pendingArticles = Collections.emptyList();

    private List<User> pendingUsers = Collections.emptyList();

    private TagSummary tagSummary = new TagSummary();

    @Data
    public static class Metrics {
        private long totalArticles;

        private long draftArticles;

        private long pendingArticles;

        private long publishedArticles;

        private long rejectedArticles;

        private long publicPublishedArticles;

        private long privateArticles;

        private long totalUsers;

        private long pendingUsers;

        private long activeUsers;

        private long disabledUsers;

        private long totalTags;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusCount {
        private String key;

        private String label;

        private String tone;

        private long count;

        private long percent;
    }

    @Data
    public static class TagSummary {
        private long total;

        private List<Tag> items = Collections.emptyList();
    }
}
