package com.blog.service;

import com.blog.common.BusinessException;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.mapper.ArticleFavoriteMapper;
import com.blog.service.impl.FavoriteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

        verify(favoriteMapper).upsertFavorite(
                anyLong(), eq(10L), eq(20L), eq("Visible title"), eq("alice"), any(LocalDateTime.class));
    }

    @Test
    void favoriteArticle_shouldHidePublicLookupFailure() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        when(articleService.getPublicArticleSummary(20L))
                .thenThrow(new BusinessException("文章不存在"));

        assertThatThrownBy(() -> favoriteService.favoriteArticle(20L, "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文章不存在或暂不可收藏");
    }

    @Test
    void favoriteStatusAndCancel_shouldUseOnlyCurrentUserId() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        when(favoriteMapper.countActiveFavorite(10L, 20L)).thenReturn(1L);

        assertThat(favoriteService.getFavoriteStatus(20L, "alice").isFavorited()).isTrue();
        favoriteService.unfavoriteArticle(20L, "alice");

        verify(favoriteMapper).cancelFavorite(eq(10L), eq(20L), eq("alice"), any(LocalDateTime.class));
    }

    @Test
    void favoriteArticle_shouldRejectMissingCurrentUserBeforeArticleLookup() {
        when(userService.getCurrentUser("missing")).thenReturn(null);

        assertThatThrownBy(() -> favoriteService.favoriteArticle(20L, "missing"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");
        verify(articleService, never()).getPublicArticleSummary(anyLong());
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
