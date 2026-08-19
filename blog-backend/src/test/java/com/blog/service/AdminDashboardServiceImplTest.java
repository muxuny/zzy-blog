package com.blog.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.ArticleStatus;
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
import com.blog.service.impl.AdminDashboardServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminDashboardServiceImplTest {

    private final ArticleService articleService = mock(ArticleService.class);

    private final UserService userService = mock(UserService.class);

    private final TagService tagService = mock(TagService.class);

    private final ArticleMapper articleMapper = mock(ArticleMapper.class);

    private final ArticleReadingHistoryMapper readingHistoryMapper =
            mock(ArticleReadingHistoryMapper.class);

    private final ArticleFavoriteMapper favoriteMapper = mock(ArticleFavoriteMapper.class);

    private final ImageMapper imageMapper = mock(ImageMapper.class);

    private final ImageService imageService = mock(ImageService.class);

    private final AdminDashboardService service = new AdminDashboardServiceImpl(
            articleService, userService, tagService, articleMapper, readingHistoryMapper,
            favoriteMapper, imageMapper, imageService);

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                Article.class);
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                User.class);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void getOverviewBuildsFullSiteStatsAndSmallReviewQueues() {
        Article pendingArticle = new Article();
        pendingArticle.setId(10L);
        pendingArticle.setTitle("待审核文章");
        pendingArticle.setStatus(ArticleStatus.PENDING);
        Page<Article> pendingArticles = new Page<>(1, 5);
        pendingArticles.setTotal(2);
        pendingArticles.setRecords(Collections.singletonList(pendingArticle));
        when(articleService.getAdminPage(any(ArticlePageQuery.class))).thenReturn(pendingArticles);
        when(articleService.count()).thenReturn(7L);
        when(articleService.count(any(Wrapper.class))).thenReturn(2L, 2L, 2L, 1L, 1L, 1L);

        Article topArticle = new Article();
        topArticle.setId(40L);
        topArticle.setTitle("热门文章");
        topArticle.setViewCount(800);
        Page<Article> topPage = new Page<>(1, 5);
        topPage.setTotal(1);
        topPage.setRecords(Collections.singletonList(topArticle));
        when(articleService.page(any(Page.class), any(Wrapper.class))).thenReturn(topPage);
        when(articleMapper.selectTotalViewCount()).thenReturn(1200L);
        when(articleMapper.selectLowViewArticleCount(anyLong())).thenReturn(2L);

        User pendingUser = new User();
        pendingUser.setId(20L);
        pendingUser.setUsername("new-user");
        pendingUser.setPassword("secret");
        Page<User> pendingUsers = new Page<>(1, 5);
        pendingUsers.setTotal(1);
        pendingUsers.setRecords(Collections.singletonList(pendingUser));
        when(userService.count()).thenReturn(2L);
        when(userService.count(any(Wrapper.class))).thenReturn(1L, 1L, 0L);
        when(userService.page(any(Page.class), any(Wrapper.class))).thenReturn(pendingUsers);

        Tag tag = new Tag();
        tag.setId(30L);
        tag.setName("Vue");
        Page<Tag> tags = new Page<>(1, 12);
        tags.setTotal(3);
        tags.setRecords(Collections.singletonList(tag));
        when(tagService.count()).thenReturn(3L);
        when(tagService.getAdminPage(1, 12)).thenReturn(tags);

        when(readingHistoryMapper.selectRecentReadingCount(any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(96L, 384L);
        when(readingHistoryMapper.selectActiveReaderCount(any(LocalDateTime.class))).thenReturn(41L);
        when(readingHistoryMapper.selectAverageProgress(any(LocalDateTime.class))).thenReturn(62L);
        when(readingHistoryMapper.selectReadingProgressBuckets(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(new DashboardProgressBucket("阅读中", 46, 0)));
        when(readingHistoryMapper.selectReadingDateBuckets(any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(new DashboardDateBucket("2026-08-06", 3)));

        when(favoriteMapper.selectTotalFavoriteCount()).thenReturn(318L);
        when(favoriteMapper.selectRecentFavoriteCount(any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(24L);
        when(favoriteMapper.selectFavoriteTopArticles())
                .thenReturn(Collections.singletonList(
                        new DashboardFavoriteTopArticle(50L, "收藏文章", 12)));
        when(favoriteMapper.selectFavoriteDateBuckets(any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(new DashboardDateBucket("2026-08-06", 3)));

        when(imageService.count()).thenReturn(246L);
        when(imageMapper.selectTotalImageSize()).thenReturn(2576980480L);
        when(imageMapper.selectRecentImageCount(any(LocalDateTime.class))).thenReturn(18L);

        AdminDashboardOverview overview = service.getOverview();

        assertThat(overview.getMetrics().getTotalArticles()).isEqualTo(7);
        assertThat(overview.getMetrics().getDraftArticles()).isEqualTo(2);
        assertThat(overview.getMetrics().getPendingArticles()).isEqualTo(2);
        assertThat(overview.getMetrics().getPublishedArticles()).isEqualTo(2);
        assertThat(overview.getMetrics().getRejectedArticles()).isEqualTo(1);
        assertThat(overview.getMetrics().getPublicPublishedArticles()).isEqualTo(1);
        assertThat(overview.getMetrics().getPrivateArticles()).isEqualTo(1);
        assertThat(overview.getMetrics().getTotalUsers()).isEqualTo(2);
        assertThat(overview.getMetrics().getPendingUsers()).isEqualTo(1);
        assertThat(overview.getMetrics().getActiveUsers()).isEqualTo(1);
        assertThat(overview.getMetrics().getDisabledUsers()).isEqualTo(0);
        assertThat(overview.getMetrics().getTotalTags()).isEqualTo(3);
        assertThat(overview.getArticleStatus()).extracting(AdminDashboardOverview.StatusCount::getKey)
                .containsExactly("draft", "pending", "published", "rejected");
        assertThat(overview.getPendingArticles()).extracting(Article::getTitle)
                .containsExactly("待审核文章");
        assertThat(overview.getPendingUsers()).extracting(User::getUsername)
                .containsExactly("new-user");
        assertThat(overview.getPendingUsers().get(0).getPassword()).isNull();
        assertThat(overview.getTagSummary().getTotal()).isEqualTo(3);
        assertThat(overview.getTagSummary().getItems()).extracting(Tag::getName)
                .containsExactly("Vue");

        assertThat(overview.getTrafficSummary().getTotalViews()).isEqualTo(1200);
        assertThat(overview.getTrafficSummary().getAverageViews()).isEqualTo(600);
        assertThat(overview.getTrafficSummary().getLowViewArticles()).isEqualTo(2);
        assertThat(overview.getTrafficSummary().getTopArticles())
                .extracting(DashboardTopArticle::getTitle).containsExactly("热门文章");
        assertThat(overview.getReadingSummary().getRecent7Days()).isEqualTo(96);
        assertThat(overview.getReadingSummary().getRecent30Days()).isEqualTo(384);
        assertThat(overview.getReadingSummary().getActiveReaders30Days()).isEqualTo(41);
        assertThat(overview.getReadingSummary().getAverageProgress()).isEqualTo(62);
        assertThat(overview.getReadingSummary().getProgressBuckets())
                .extracting(DashboardProgressBucket::getLabel).containsExactly(
                        "浅读", "阅读中", "接近读完", "已读完");
        assertThat(overview.getReadingSummary().getDailyReads()).hasSize(30);
        assertThat(overview.getFavoriteSummary().getTotal()).isEqualTo(318);
        assertThat(overview.getFavoriteSummary().getRecent7Days()).isEqualTo(24);
        assertThat(overview.getFavoriteSummary().getTopArticles())
                .extracting(DashboardFavoriteTopArticle::getTitle).containsExactly("收藏文章");
        assertThat(overview.getFavoriteSummary().getDailyFavorites()).hasSize(7);
        assertThat(overview.getResourceSummary().getTotalImages()).isEqualTo(246);
        assertThat(overview.getResourceSummary().getTotalImageSize()).isEqualTo(2576980480L);
        assertThat(overview.getResourceSummary().getRecent7DaysImages()).isEqualTo(18);
        assertThat(overview.getResourceSummary().getTotalTags()).isEqualTo(3);

        ArgumentCaptor<ArticlePageQuery> articleQueryCaptor =
                ArgumentCaptor.forClass(ArticlePageQuery.class);
        verify(articleService).getAdminPage(articleQueryCaptor.capture());
        assertThat(articleQueryCaptor.getValue().getStatus()).isEqualTo(ArticleStatus.PENDING);
        assertThat(articleQueryCaptor.getValue().getPage()).isEqualTo(1);
        assertThat(articleQueryCaptor.getValue().getSize()).isEqualTo(5);
        verify(articleService, never()).list();
        verify(userService, never()).list();
    }
}
