package com.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.BusinessException;
import com.blog.dto.FavoriteArticleItem;
import com.blog.dto.FavoritePageQuery;
import com.blog.dto.FavoriteRelationRow;
import com.blog.entity.Article;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.mapper.ArticleFavoriteMapper;
import com.blog.service.impl.FavoriteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class FavoriteServiceImplTest {

    private ArticleFavoriteMapper favoriteMapper;
    private ArticleService articleService;
    private UserService userService;
    private FavoriteService favoriteService;

    @BeforeEach
    void setUp() {
        favoriteMapper = mock(ArticleFavoriteMapper.class);
        articleService = mock(ArticleService.class);
        userService = mock(UserService.class);
        favoriteService = new FavoriteServiceImpl(favoriteMapper, articleService, userService);
    }

    @Test
    void favoriteArticle_shouldSavePublicTitleForCurrentUser() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        Article article = new Article();
        article.setId(20L);
        article.setTitle("Visible title");
        when(articleService.getPublicArticleSummary(20L)).thenReturn(article);

        favoriteService.favoriteArticle(20L, "alice");

        verify(userService).getCurrentUser("alice");
        verify(articleService).getPublicArticleSummary(20L);
        verify(favoriteMapper).upsertFavorite(
                anyLong(), eq(10L), eq(20L), eq("Visible title"), eq("alice"), any(LocalDateTime.class));
        verifyNoMoreInteractions(userService, articleService, favoriteMapper);
    }

    @Test
    void favoriteArticle_shouldHidePublicLookupFailure() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        when(articleService.getPublicArticleSummary(20L))
                .thenThrow(new BusinessException("文章不存在"));

        assertThatThrownBy(() -> favoriteService.favoriteArticle(20L, "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文章不存在或暂不可收藏");
        verify(userService).getCurrentUser("alice");
        verify(articleService).getPublicArticleSummary(20L);
        verifyNoInteractions(favoriteMapper);
        verifyNoMoreInteractions(userService, articleService);
    }

    @Test
    void getFavoriteStatus_shouldCountOnlyCurrentUserFavorites() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        when(favoriteMapper.countActiveFavorite(10L, 20L)).thenReturn(1L);

        assertThat(favoriteService.getFavoriteStatus(20L, "alice").isFavorited()).isTrue();

        verify(userService).getCurrentUser("alice");
        verify(favoriteMapper).countActiveFavorite(10L, 20L);
        verifyNoInteractions(articleService);
        verifyNoMoreInteractions(userService, favoriteMapper);
    }

    @Test
    void getMyFavorites_shouldReturnPublicArticleAndSafeUnavailableSnapshot() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        LocalDateTime availableFavoritedAt = LocalDateTime.of(2026, 7, 15, 9, 30);
        LocalDateTime unavailableFavoritedAt = LocalDateTime.of(2026, 7, 14, 8, 20);
        FavoriteRelationRow available = relation(
                1L, 20L, "Old public title", availableFavoritedAt, true);
        FavoriteRelationRow unavailable = relation(
                2L, 21L, "Saved private title", unavailableFavoritedAt, false);
        Page<FavoriteRelationRow> rows = new Page<>(2, 5);
        rows.setTotal(2);
        rows.setRecords(Arrays.asList(available, unavailable));
        when(favoriteMapper.selectFavoritePage(any(Page.class), eq(10L), eq("architecture"), eq(3L)))
                .thenReturn(rows);

        Article current = new Article();
        current.setId(20L);
        current.setTitle("Current public title");
        current.setSummary("Public summary");
        current.setCoverImage("public-cover.png");
        current.setAuthorName("Public author");
        current.setViewCount(42);
        current.setTags(Collections.singletonList(tag(3L, "Java")));
        when(articleService.getPublicArticleSummaries(Collections.singletonList(20L)))
                .thenReturn(Collections.singletonList(current));
        FavoritePageQuery query = new FavoritePageQuery();
        query.setPage(2);
        query.setSize(5);
        query.setKeyword("architecture");
        query.setTagId(3L);

        IPage<FavoriteArticleItem> result = favoriteService.getMyFavorites(query, "alice");

        assertThat(result.getCurrent()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getTotal()).isEqualTo(2);
        FavoriteArticleItem visible = result.getRecords().get(0);
        assertThat(visible.isAvailable()).isTrue();
        assertThat(visible.getTitle()).isEqualTo("Current public title");
        assertThat(visible.getSummary()).isEqualTo("Public summary");
        assertThat(visible.getCoverImage()).isEqualTo("public-cover.png");
        assertThat(visible.getAuthorName()).isEqualTo("Public author");
        assertThat(visible.getViewCount()).isEqualTo(42);
        assertThat(visible.getTags()).extracting(Tag::getName).containsExactly("Java");
        assertThat(visible.getFavoritedAt()).isEqualTo(availableFavoritedAt);

        FavoriteArticleItem hidden = result.getRecords().get(1);
        assertThat(hidden.getArticleId()).isEqualTo(21L);
        assertThat(hidden.isAvailable()).isFalse();
        assertThat(hidden.getTitle()).isEqualTo("Saved private title");
        assertThat(hidden.getSummary()).isNull();
        assertThat(hidden.getCoverImage()).isNull();
        assertThat(hidden.getAuthorName()).isNull();
        assertThat(hidden.getViewCount()).isNull();
        assertThat(hidden.getTags()).isEmpty();
        assertThat(hidden.getFavoritedAt()).isEqualTo(unavailableFavoritedAt);
        assertThat(hidden.getUnavailableMessage()).isEqualTo("该文章暂未公开");
        ArgumentCaptor<Page<FavoriteRelationRow>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(favoriteMapper).selectFavoritePage(pageCaptor.capture(), eq(10L),
                eq("architecture"), eq(3L));
        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(2);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(5);
    }

    @Test
    void getMyFavorites_shouldSafelyFallbackWhenAvailableArticleDisappears() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        LocalDateTime favoritedAt = LocalDateTime.of(2026, 7, 15, 10, 45);
        FavoriteRelationRow available = relation(
                1L, 20L, "Saved public title", favoritedAt, true);
        Page<FavoriteRelationRow> rows = new Page<>(2, 5);
        rows.setTotal(6);
        rows.setRecords(Collections.singletonList(available));
        when(favoriteMapper.selectFavoritePage(any(Page.class), eq(10L), eq(null), eq(null)))
                .thenReturn(rows);
        when(articleService.getPublicArticleSummaries(Collections.singletonList(20L)))
                .thenReturn(Collections.emptyList());
        FavoritePageQuery query = new FavoritePageQuery();
        query.setPage(2);
        query.setSize(5);

        IPage<FavoriteArticleItem> result = favoriteService.getMyFavorites(query, "alice");

        assertThat(result.getCurrent()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getTotal()).isEqualTo(6);
        FavoriteArticleItem hidden = result.getRecords().get(0);
        assertThat(hidden.getArticleId()).isEqualTo(20L);
        assertThat(hidden.getTitle()).isEqualTo("Saved public title");
        assertThat(hidden.getFavoritedAt()).isEqualTo(favoritedAt);
        assertThat(hidden.isAvailable()).isFalse();
        assertThat(hidden.getSummary()).isNull();
        assertThat(hidden.getCoverImage()).isNull();
        assertThat(hidden.getAuthorName()).isNull();
        assertThat(hidden.getViewCount()).isNull();
        assertThat(hidden.getTags()).isEmpty();
        assertThat(hidden.getUnavailableMessage()).isEqualTo("该文章暂未公开");
    }

    @Test
    void getMyFavorites_shouldReturnUnavailableOnlyPageWithoutPublicDetails() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        LocalDateTime favoritedAt = LocalDateTime.of(2026, 7, 13, 7, 10);
        FavoriteRelationRow unavailable = relation(
                1L, 20L, "Saved unavailable title", favoritedAt, false);
        Page<FavoriteRelationRow> rows = new Page<>(1, 10);
        rows.setTotal(1);
        rows.setRecords(Collections.singletonList(unavailable));
        when(favoriteMapper.selectFavoritePage(any(Page.class), eq(10L), eq(null), eq(null)))
                .thenReturn(rows);
        when(articleService.getPublicArticleSummaries(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        IPage<FavoriteArticleItem> result = favoriteService.getMyFavorites(
                new FavoritePageQuery(), "alice");

        assertThat(result.getTotal()).isEqualTo(1);
        FavoriteArticleItem hidden = result.getRecords().get(0);
        assertThat(hidden.getTitle()).isEqualTo("Saved unavailable title");
        assertThat(hidden.getFavoritedAt()).isEqualTo(favoritedAt);
        assertThat(hidden.isAvailable()).isFalse();
        assertThat(hidden.getTags()).isEmpty();
        assertThat(Arrays.asList(hidden.getSummary(), hidden.getCoverImage(),
                hidden.getAuthorName(), hidden.getViewCount())).containsOnlyNulls();
        verify(articleService).getPublicArticleSummaries(Collections.emptyList());
    }

    @Test
    void getMyFavorites_shouldRejectNullQueryBeforeUsingDependencies() {
        assertInvalidFavoritePage(null);
    }

    @Test
    void getMyFavorites_shouldRejectPageBeforeFirstPage() {
        FavoritePageQuery query = new FavoritePageQuery();
        query.setPage(0);

        assertInvalidFavoritePage(query);
    }

    @Test
    void getMyFavorites_shouldRejectNonPositivePageSize() {
        FavoritePageQuery query = new FavoritePageQuery();
        query.setSize(0);

        assertInvalidFavoritePage(query);
    }

    @Test
    void getMyFavorites_shouldRejectPageSizeAboveLimit() {
        FavoritePageQuery query = new FavoritePageQuery();
        query.setSize(101);

        assertInvalidFavoritePage(query);
    }

    @Test
    void unfavoriteArticle_shouldCancelOnlyCurrentUserFavorite() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));

        favoriteService.unfavoriteArticle(20L, "alice");

        verify(userService).getCurrentUser("alice");
        verify(favoriteMapper).cancelFavorite(eq(10L), eq(20L), eq("alice"), any(LocalDateTime.class));
        verifyNoInteractions(articleService);
        verifyNoMoreInteractions(userService, favoriteMapper);
    }

    @Test
    void favoriteArticle_shouldRejectMissingCurrentUserBeforeArticleLookup() {
        when(userService.getCurrentUser("missing")).thenReturn(null);

        assertThatThrownBy(() -> favoriteService.favoriteArticle(20L, "missing"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");
        verify(userService).getCurrentUser("missing");
        verifyNoInteractions(articleService, favoriteMapper);
        verifyNoMoreInteractions(userService);
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private static FavoriteRelationRow relation(Long favoriteId, Long articleId,
                                                String titleSnapshot,
                                                LocalDateTime favoritedAt,
                                                boolean available) {
        FavoriteRelationRow row = new FavoriteRelationRow();
        row.setFavoriteId(favoriteId);
        row.setArticleId(articleId);
        row.setTitleSnapshot(titleSnapshot);
        row.setFavoritedAt(favoritedAt);
        row.setAvailable(available);
        return row;
    }

    private static Tag tag(Long id, String name) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tag;
    }

    private void assertInvalidFavoritePage(FavoritePageQuery query) {
        assertThatThrownBy(() -> favoriteService.getMyFavorites(query, "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分页参数不合法");
        verifyNoInteractions(userService, favoriteMapper, articleService);
    }
}
