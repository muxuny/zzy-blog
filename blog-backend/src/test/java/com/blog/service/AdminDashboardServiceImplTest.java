package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.ArticleStatus;
import com.blog.dto.AdminDashboardOverview;
import com.blog.dto.ArticlePageQuery;
import com.blog.entity.Article;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.service.impl.AdminDashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminDashboardServiceImplTest {

    private final ArticleService articleService = mock(ArticleService.class);

    private final UserService userService = mock(UserService.class);

    private final TagService tagService = mock(TagService.class);

    private final AdminDashboardService service = new AdminDashboardServiceImpl(
            articleService, userService, tagService);

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

        ArgumentCaptor<ArticlePageQuery> articleQueryCaptor = ArgumentCaptor.forClass(ArticlePageQuery.class);
        verify(articleService).getAdminPage(articleQueryCaptor.capture());
        assertThat(articleQueryCaptor.getValue().getStatus()).isEqualTo(ArticleStatus.PENDING);
        assertThat(articleQueryCaptor.getValue().getPage()).isEqualTo(1);
        assertThat(articleQueryCaptor.getValue().getSize()).isEqualTo(5);
        verify(articleService, never()).list();
        verify(userService, never()).list();
    }
}
