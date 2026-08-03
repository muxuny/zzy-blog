package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.ArticleStatus;
import com.blog.common.ArticleVisibility;
import com.blog.dto.AdminDashboardOverview;
import com.blog.dto.ArticlePageQuery;
import com.blog.entity.Article;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.service.AdminDashboardService;
import com.blog.service.ArticleService;
import com.blog.service.TagService;
import com.blog.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {
    private static final int REVIEW_PREVIEW_SIZE = 5;

    private static final int TAG_PREVIEW_SIZE = 12;

    private static final String USER_PENDING = "pending";

    private static final String USER_ACTIVE = "active";

    private static final String USER_DISABLED = "disabled";

    private final ArticleService articleService;

    private final UserService userService;

    private final TagService tagService;

    public AdminDashboardServiceImpl(ArticleService articleService,
                                     UserService userService,
                                     TagService tagService) {
        this.articleService = articleService;
        this.userService = userService;
        this.tagService = tagService;
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

        overview.setArticleStatus(buildArticleStatus(metrics));
        overview.setPendingArticles(listPendingArticles());
        overview.setPendingUsers(listPendingUsers());
        overview.setTagSummary(buildTagSummary(metrics.getTotalTags()));
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

    private List<AdminDashboardOverview.StatusCount> buildArticleStatus(AdminDashboardOverview.Metrics metrics) {
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
}
