package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.ArticleStatus;
import com.blog.common.ArticleVisibility;
import com.blog.dto.AdminDashboardOverview;
import com.blog.dto.ArticlePageQuery;
import com.blog.dto.DashboardDateBucket;
import com.blog.dto.DashboardFavoriteTopArticle;
import com.blog.dto.DashboardProgressBucket;
import com.blog.dto.DashboardTopArticle;
import com.blog.entity.Article;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.mapper.ArticleFavoriteMapper;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleReadingHistoryMapper;
import com.blog.mapper.ImageMapper;
import com.blog.service.AdminDashboardService;
import com.blog.service.ArticleService;
import com.blog.service.ImageService;
import com.blog.service.TagService;
import com.blog.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {
    private static final int REVIEW_PREVIEW_SIZE = 5;

    private static final int TAG_PREVIEW_SIZE = 12;

    private static final int TOP_ARTICLES_SIZE = 5;

    private static final int READING_DAYS = 30;

    private static final int FAVORITE_DAYS = 7;

    private static final String USER_PENDING = "pending";

    private static final String USER_ACTIVE = "active";

    private static final String USER_DISABLED = "disabled";

    private final ArticleService articleService;

    private final UserService userService;

    private final TagService tagService;

    private final ArticleMapper articleMapper;

    private final ArticleReadingHistoryMapper readingHistoryMapper;

    private final ArticleFavoriteMapper favoriteMapper;

    private final ImageMapper imageMapper;

    private final ImageService imageService;

    public AdminDashboardServiceImpl(ArticleService articleService,
                                     UserService userService,
                                     TagService tagService,
                                     ArticleMapper articleMapper,
                                     ArticleReadingHistoryMapper readingHistoryMapper,
                                     ArticleFavoriteMapper favoriteMapper,
                                     ImageMapper imageMapper,
                                     ImageService imageService) {
        this.articleService = articleService;
        this.userService = userService;
        this.tagService = tagService;
        this.articleMapper = articleMapper;
        this.readingHistoryMapper = readingHistoryMapper;
        this.favoriteMapper = favoriteMapper;
        this.imageMapper = imageMapper;
        this.imageService = imageService;
    }

    @Override
    public AdminDashboardOverview getOverview() {
        AdminDashboardOverview overview = new AdminDashboardOverview();
        AdminDashboardOverview.Metrics metrics = overview.getMetrics();
        metrics.setTotalArticles(articleService.count());
        metrics.setDraftArticles(countArticlesByStatus(ArticleStatus.DRAFT));
        metrics.setPendingArticles(countArticlesByStatus(ArticleStatus.PENDING));
        metrics.setPublishedArticles(countArticlesByStatus(ArticleStatus.PUBLISHED));
        metrics.setRejectedArticles(countArticlesByStatus(ArticleStatus.REJECTED));
        metrics.setPublicPublishedArticles(countPublicPublishedArticles());
        metrics.setPrivateArticles(countPrivatePublishedArticles());
        metrics.setTotalUsers(userService.count());
        metrics.setPendingUsers(countUsersByStatus(USER_PENDING));
        metrics.setActiveUsers(countUsersByStatus(USER_ACTIVE));
        metrics.setDisabledUsers(countUsersByStatus(USER_DISABLED));
        metrics.setTotalTags(tagService.count());

        LocalDateTime now = LocalDateTime.now();
        overview.setArticleStatus(buildArticleStatus(metrics));
        overview.setPendingArticles(listPendingArticles());
        overview.setPendingUsers(listPendingUsers());
        overview.setTagSummary(buildTagSummary(metrics.getTotalTags()));
        overview.setTrafficSummary(buildTrafficSummary(metrics.getPublishedArticles()));
        overview.setReadingSummary(buildReadingSummary(now));
        overview.setFavoriteSummary(buildFavoriteSummary(now));
        overview.setResourceSummary(buildResourceSummary(now, metrics.getTotalTags()));
        return overview;
    }

    private long countArticlesByStatus(String status) {
        return articleService.count(new LambdaQueryWrapper<Article>().eq(Article::getStatus, status));
    }

    private long countPublicPublishedArticles() {
        return articleService.count(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, ArticleStatus.PUBLISHED)
                .and(wrapper -> wrapper.eq(Article::getVisibility, ArticleVisibility.PUBLIC)
                        .or()
                        .isNull(Article::getVisibility)));
    }

    private long countPrivatePublishedArticles() {
        return articleService.count(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, ArticleStatus.PUBLISHED)
                .eq(Article::getVisibility, ArticleVisibility.PRIVATE));
    }

    private long countUsersByStatus(String status) {
        return userService.count(new LambdaQueryWrapper<User>().eq(User::getStatus, status));
    }

    private List<AdminDashboardOverview.StatusCount> buildArticleStatus(
            AdminDashboardOverview.Metrics metrics) {
        long total = metrics.getTotalArticles();
        return Arrays.asList(
                status(ArticleStatus.DRAFT, "草稿", "neutral", metrics.getDraftArticles(), total),
                status(ArticleStatus.PENDING, "待审核", "warning", metrics.getPendingArticles(), total),
                status(ArticleStatus.PUBLISHED, "已发布", "success", metrics.getPublishedArticles(), total),
                status(ArticleStatus.REJECTED, "已驳回", "danger", metrics.getRejectedArticles(), total));
    }

    private AdminDashboardOverview.StatusCount status(String key, String label, String tone,
                                                      long count, long total) {
        long percent = total == 0 ? 0 : Math.round(count * 100.0 / total);
        return new AdminDashboardOverview.StatusCount(key, label, tone, count, percent);
    }

    private List<Article> listPendingArticles() {
        ArticlePageQuery query = new ArticlePageQuery();
        query.setPage(1);
        query.setSize(REVIEW_PREVIEW_SIZE);
        query.setStatus(ArticleStatus.PENDING);
        return articleService.getAdminPage(query).getRecords();
    }

    private List<User> listPendingUsers() {
        Page<User> page = new Page<>(1, REVIEW_PREVIEW_SIZE);
        List<User> records = userService.page(page,
                new LambdaQueryWrapper<User>()
                        .eq(User::getStatus, USER_PENDING)
                        .orderByDesc(User::getCreatedAt)
                        .orderByDesc(User::getId)).getRecords();
        records.forEach(user -> user.setPassword(null));
        return records;
    }

    private AdminDashboardOverview.TagSummary buildTagSummary(long totalTags) {
        IPage<Tag> page = tagService.getAdminPage(1, TAG_PREVIEW_SIZE);
        AdminDashboardOverview.TagSummary summary = new AdminDashboardOverview.TagSummary();
        summary.setTotal(totalTags);
        summary.setItems(page.getRecords());
        return summary;
    }

    private AdminDashboardOverview.TrafficSummary buildTrafficSummary(long publishedArticles) {
        long totalViews = articleMapper.selectTotalViewCount();
        long averageViews = publishedArticles == 0 ? 0 : totalViews / publishedArticles;
        AdminDashboardOverview.TrafficSummary summary = new AdminDashboardOverview.TrafficSummary();
        summary.setTotalViews(totalViews);
        summary.setAverageViews(averageViews);
        summary.setLowViewArticles(articleMapper.selectLowViewArticleCount(averageViews));

        Page<Article> topPage = articleService.page(new Page<>(1, TOP_ARTICLES_SIZE),
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, ArticleStatus.PUBLISHED)
                        .and(wrapper -> wrapper.eq(Article::getVisibility, ArticleVisibility.PUBLIC)
                                .or()
                                .isNull(Article::getVisibility))
                        .select(Article::getId, Article::getTitle, Article::getViewCount)
                        .orderByDesc(Article::getViewCount)
                        .orderByDesc(Article::getId));
        summary.setTopArticles(topPage.getRecords().stream()
                .map(article -> new DashboardTopArticle(
                        article.getId(),
                        article.getTitle(),
                        article.getViewCount() == null ? 0 : article.getViewCount()))
                .collect(Collectors.toList()));
        return summary;
    }

    private AdminDashboardOverview.ReadingSummary buildReadingSummary(LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalDate startDate = today.minusDays(READING_DAYS - 1L);
        LocalDateTime start7 = now.minusDays(7);
        LocalDateTime start30 = startDate.atStartOfDay();
        LocalDateTime endExclusive = today.plusDays(1).atStartOfDay();

        AdminDashboardOverview.ReadingSummary summary = new AdminDashboardOverview.ReadingSummary();
        summary.setRecent7Days(readingHistoryMapper.selectRecentReadingCount(start7, now));
        summary.setRecent30Days(readingHistoryMapper.selectRecentReadingCount(start30, now));
        summary.setActiveReaders30Days(readingHistoryMapper.selectActiveReaderCount(start30));
        summary.setAverageProgress(readingHistoryMapper.selectAverageProgress(start30));
        summary.setProgressBuckets(buildProgressBuckets(
                readingHistoryMapper.selectReadingProgressBuckets(start30),
                summary.getRecent30Days()));
        summary.setDailyReads(fillDateBuckets(startDate, today,
                readingHistoryMapper.selectReadingDateBuckets(start30, endExclusive)));
        return summary;
    }

    private AdminDashboardOverview.FavoriteSummary buildFavoriteSummary(LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalDate startDate = today.minusDays(FAVORITE_DAYS - 1L);
        LocalDateTime start7 = startDate.atStartOfDay();
        LocalDateTime endExclusive = today.plusDays(1).atStartOfDay();

        AdminDashboardOverview.FavoriteSummary summary = new AdminDashboardOverview.FavoriteSummary();
        summary.setTotal(favoriteMapper.selectTotalFavoriteCount());
        summary.setRecent7Days(favoriteMapper.selectRecentFavoriteCount(start7, now));
        summary.setTopArticles(favoriteMapper.selectFavoriteTopArticles());
        summary.setDailyFavorites(fillDateBuckets(startDate, today,
                favoriteMapper.selectFavoriteDateBuckets(start7, endExclusive)));
        return summary;
    }

    private AdminDashboardOverview.ResourceSummary buildResourceSummary(
            LocalDateTime now, long totalTags) {
        AdminDashboardOverview.ResourceSummary summary = new AdminDashboardOverview.ResourceSummary();
        summary.setTotalImages(imageService.count());
        summary.setTotalImageSize(imageMapper.selectTotalImageSize());
        summary.setRecent7DaysImages(imageMapper.selectRecentImageCount(now.minusDays(7)));
        summary.setTotalTags(totalTags);
        return summary;
    }

    private List<DashboardProgressBucket> buildProgressBuckets(
            List<DashboardProgressBucket> source, long total) {
        Map<String, Long> countByLabel = source.stream().collect(Collectors.toMap(
                DashboardProgressBucket::getLabel,
                DashboardProgressBucket::getCount,
                Long::sum));
        List<String> labels = Arrays.asList("浅读", "阅读中", "接近读完", "已读完");
        return labels.stream()
                .map(label -> {
                    long count = countByLabel.getOrDefault(label, 0L);
                    long percent = total == 0 ? 0 : Math.round(count * 100.0 / total);
                    return new DashboardProgressBucket(label, count, percent);
                })
                .collect(Collectors.toList());
    }

    private List<DashboardDateBucket> fillDateBuckets(
            LocalDate start, LocalDate end, List<DashboardDateBucket> source) {
        Map<String, Long> countByDate = source.stream().collect(Collectors.toMap(
                DashboardDateBucket::getDate,
                DashboardDateBucket::getCount,
                Long::sum,
                LinkedHashMap::new));
        List<DashboardDateBucket> buckets = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String key = date.toString();
            buckets.add(new DashboardDateBucket(key, countByDate.getOrDefault(key, 0L)));
        }
        return buckets;
    }
}
