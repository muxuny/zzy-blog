package com.blog.dto;

import com.blog.dto.DashboardDateBucket;
import com.blog.dto.DashboardFavoriteTopArticle;
import com.blog.dto.DashboardProgressBucket;
import com.blog.dto.DashboardTopArticle;
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

    private TrafficSummary trafficSummary = new TrafficSummary();

    private ReadingSummary readingSummary = new ReadingSummary();

    private FavoriteSummary favoriteSummary = new FavoriteSummary();

    private ResourceSummary resourceSummary = new ResourceSummary();

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

    @Data
    public static class TrafficSummary {
        private long totalViews;

        private long averageViews;

        private long lowViewArticles;

        private List<DashboardTopArticle> topArticles = Collections.emptyList();
    }

    @Data
    public static class ReadingSummary {
        private long recent7Days;

        private long recent30Days;

        private long activeReaders30Days;

        private long averageProgress;

        private List<DashboardProgressBucket> progressBuckets = Collections.emptyList();

        private List<DashboardDateBucket> dailyReads = Collections.emptyList();
    }

    @Data
    public static class FavoriteSummary {
        private long total;

        private long recent7Days;

        private List<DashboardFavoriteTopArticle> topArticles = Collections.emptyList();

        private List<DashboardDateBucket> dailyFavorites = Collections.emptyList();
    }

    @Data
    public static class ResourceSummary {
        private long totalImages;

        private long totalImageSize;

        private long recent7DaysImages;

        private long totalTags;
    }
}
