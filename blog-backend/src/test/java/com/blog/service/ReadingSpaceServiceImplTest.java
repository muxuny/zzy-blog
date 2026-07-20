package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.BusinessException;
import com.blog.dto.FavoriteArticleItem;
import com.blog.dto.FavoritePageQuery;
import com.blog.dto.ReadingHistoryItem;
import com.blog.dto.ReadingHistoryOverview;
import com.blog.dto.ReadingOverview;
import com.blog.entity.User;
import com.blog.service.impl.ReadingSpaceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ReadingSpaceServiceImplTest {

    private ReadingHistoryService historyService;
    private FavoriteService favoriteService;
    private UserService userService;
    private ReadingSpaceService readingSpaceService;

    @BeforeEach
    void setUp() {
        historyService = mock(ReadingHistoryService.class);
        favoriteService = mock(FavoriteService.class);
        userService = mock(UserService.class);
        readingSpaceService = new ReadingSpaceServiceImpl(historyService, favoriteService, userService);
    }

    @Test
    void getOverview_shouldResolveUserOnceAndUseOverviewSpecificDependencies() {
        ReadingHistoryItem lastRead = historyItem(100L);
        Page<ReadingHistoryItem> history = new Page<>(1, 5);
        history.setTotal(12);
        history.setRecords(Arrays.asList(historyItem(1L), historyItem(2L), historyItem(3L),
                historyItem(4L), historyItem(5L)));
        Page<FavoriteArticleItem> favorites = new Page<>(1, 4);
        favorites.setTotal(9);
        favorites.setRecords(Arrays.asList(favoriteItem(11L), favoriteItem(12L),
                favoriteItem(13L), favoriteItem(14L)));
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        when(historyService.getOverview(5, 10L)).thenReturn(historyOverview(lastRead, history));
        when(favoriteService.getMyFavoritesForUser(
                org.mockito.ArgumentMatchers.any(FavoritePageQuery.class),
                org.mockito.ArgumentMatchers.eq(10L))).thenReturn(favorites);

        ReadingOverview result = readingSpaceService.getOverview("alice");

        assertThat(result.getLastRead()).isSameAs(lastRead);
        assertThat(result.getRecentHistory()).isSameAs(history.getRecords());
        assertThat(result.getHistoryTotal()).isEqualTo(12);
        assertThat(result.getRecentFavorites()).isSameAs(favorites.getRecords());
        assertThat(result.getFavoriteTotal()).isEqualTo(9);

        ArgumentCaptor<FavoritePageQuery> favoriteQuery =
                ArgumentCaptor.forClass(FavoritePageQuery.class);
        verify(userService).getCurrentUser("alice");
        verify(historyService).getOverview(5, 10L);
        verify(favoriteService).getMyFavoritesForUser(favoriteQuery.capture(),
                org.mockito.ArgumentMatchers.eq(10L));
        assertThat(favoriteQuery.getValue().getPage()).isEqualTo(1);
        assertThat(favoriteQuery.getValue().getSize()).isEqualTo(4);
        assertThat(favoriteQuery.getValue().getKeyword()).isNull();
        assertThat(favoriteQuery.getValue().getTagId()).isNull();
        verifyNoMoreInteractions(userService, historyService, favoriteService);
    }

    @Test
    void getOverview_shouldPropagateHistoryOverviewFailureWithoutFavoriteQuery() {
        RuntimeException failure = new RuntimeException("history failed");
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        when(historyService.getOverview(5, 10L)).thenThrow(failure);

        assertThatThrownBy(() -> readingSpaceService.getOverview("alice")).isSameAs(failure);

        verify(userService).getCurrentUser("alice");
        verify(historyService).getOverview(5, 10L);
        verifyNoMoreInteractions(userService, historyService);
        verifyNoInteractions(favoriteService);
    }

    @Test
    void getOverview_shouldPropagateFavoriteFailure() {
        Page<ReadingHistoryItem> history = new Page<>(1, 5);
        RuntimeException failure = new RuntimeException("favorites failed");
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        when(historyService.getOverview(5, 10L)).thenReturn(historyOverview(null, history));
        when(favoriteService.getMyFavoritesForUser(
                org.mockito.ArgumentMatchers.any(FavoritePageQuery.class),
                org.mockito.ArgumentMatchers.eq(10L))).thenThrow(failure);

        assertThatThrownBy(() -> readingSpaceService.getOverview("alice")).isSameAs(failure);

        verify(userService).getCurrentUser("alice");
        verify(historyService).getOverview(5, 10L);
        verify(favoriteService).getMyFavoritesForUser(
                org.mockito.ArgumentMatchers.any(FavoritePageQuery.class),
                org.mockito.ArgumentMatchers.eq(10L));
        verifyNoMoreInteractions(userService, historyService, favoriteService);
    }

    @Test
    void getOverview_shouldRejectMissingUserBeforeOverviewQueries() {
        when(userService.getCurrentUser("missing")).thenReturn(null);

        assertThatThrownBy(() -> readingSpaceService.getOverview("missing"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");

        verify(userService).getCurrentUser("missing");
        verifyNoMoreInteractions(userService);
        verifyNoInteractions(historyService, favoriteService);
    }

    @Test
    void getOverview_shouldReturnNonNullEmptyListsWhenNoHistoryOrFavoritesExist() {
        Page<ReadingHistoryItem> history = new Page<>(1, 5);
        history.setTotal(0);
        history.setRecords(Collections.emptyList());
        Page<FavoriteArticleItem> favorites = new Page<>(1, 4);
        favorites.setTotal(0);
        favorites.setRecords(Collections.emptyList());
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        when(historyService.getOverview(5, 10L)).thenReturn(historyOverview(null, history));
        when(favoriteService.getMyFavoritesForUser(
                org.mockito.ArgumentMatchers.any(FavoritePageQuery.class),
                org.mockito.ArgumentMatchers.eq(10L))).thenReturn(favorites);

        ReadingOverview result = readingSpaceService.getOverview("alice");

        assertThat(result.getLastRead()).isNull();
        assertThat(result.getRecentHistory()).isNotNull().isEmpty();
        assertThat(result.getHistoryTotal()).isZero();
        assertThat(result.getRecentFavorites()).isNotNull().isEmpty();
        assertThat(result.getFavoriteTotal()).isZero();
    }

    private static ReadingHistoryOverview historyOverview(ReadingHistoryItem lastRead,
                                                           Page<ReadingHistoryItem> history) {
        ReadingHistoryOverview overview = new ReadingHistoryOverview();
        overview.setLastRead(lastRead);
        overview.setRecentHistory(history);
        return overview;
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private static ReadingHistoryItem historyItem(Long articleId) {
        ReadingHistoryItem item = new ReadingHistoryItem();
        item.setArticleId(articleId);
        return item;
    }

    private static FavoriteArticleItem favoriteItem(Long articleId) {
        FavoriteArticleItem item = new FavoriteArticleItem();
        item.setArticleId(articleId);
        return item;
    }
}
