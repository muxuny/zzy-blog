package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.FavoriteArticleItem;
import com.blog.dto.FavoritePageQuery;
import com.blog.dto.ReadingHistoryItem;
import com.blog.dto.ReadingHistoryPageQuery;
import com.blog.dto.ReadingOverview;
import com.blog.service.impl.ReadingSpaceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;

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
    private ReadingSpaceService readingSpaceService;

    @BeforeEach
    void setUp() {
        historyService = mock(ReadingHistoryService.class);
        favoriteService = mock(FavoriteService.class);
        readingSpaceService = new ReadingSpaceServiceImpl(historyService, favoriteService);
    }

    @Test
    void getOverview_shouldCombineLastReadHistoryAndFavoritesWithoutChangingPages() {
        ReadingHistoryItem lastRead = historyItem(100L);
        Page<ReadingHistoryItem> history = new Page<>(1, 5);
        history.setTotal(12);
        history.setRecords(Arrays.asList(historyItem(1L), historyItem(2L), historyItem(3L),
                historyItem(4L), historyItem(5L)));
        Page<FavoriteArticleItem> favorites = new Page<>(1, 4);
        favorites.setTotal(9);
        favorites.setRecords(Arrays.asList(favoriteItem(11L), favoriteItem(12L),
                favoriteItem(13L), favoriteItem(14L)));
        when(historyService.getHistory(org.mockito.ArgumentMatchers.any(ReadingHistoryPageQuery.class),
                org.mockito.ArgumentMatchers.eq("alice"))).thenReturn(history);
        when(favoriteService.getMyFavorites(org.mockito.ArgumentMatchers.any(FavoritePageQuery.class),
                org.mockito.ArgumentMatchers.eq("alice"))).thenReturn(favorites);
        when(historyService.getLastAvailable("alice")).thenReturn(lastRead);

        ReadingOverview result = readingSpaceService.getOverview("alice");

        assertThat(result.getLastRead()).isSameAs(lastRead);
        assertThat(result.getRecentHistory()).isSameAs(history.getRecords());
        assertThat(result.getHistoryTotal()).isEqualTo(12);
        assertThat(result.getRecentFavorites()).isSameAs(favorites.getRecords());
        assertThat(result.getFavoriteTotal()).isEqualTo(9);

        ArgumentCaptor<ReadingHistoryPageQuery> historyQuery =
                ArgumentCaptor.forClass(ReadingHistoryPageQuery.class);
        ArgumentCaptor<FavoritePageQuery> favoriteQuery =
                ArgumentCaptor.forClass(FavoritePageQuery.class);
        verify(historyService).getHistory(historyQuery.capture(), org.mockito.ArgumentMatchers.eq("alice"));
        verify(favoriteService).getMyFavorites(favoriteQuery.capture(), org.mockito.ArgumentMatchers.eq("alice"));
        verify(historyService).getLastAvailable("alice");
        assertThat(historyQuery.getValue().getPage()).isEqualTo(1);
        assertThat(historyQuery.getValue().getSize()).isEqualTo(5);
        assertThat(favoriteQuery.getValue().getPage()).isEqualTo(1);
        assertThat(favoriteQuery.getValue().getSize()).isEqualTo(4);
        assertThat(favoriteQuery.getValue().getKeyword()).isNull();
        assertThat(favoriteQuery.getValue().getTagId()).isNull();
        verifyNoMoreInteractions(historyService, favoriteService);
    }

    @Test
    void getOverview_shouldPropagateHistoryFailureWithoutPartialQueries() {
        RuntimeException failure = new RuntimeException("history failed");
        when(historyService.getHistory(org.mockito.ArgumentMatchers.any(ReadingHistoryPageQuery.class),
                org.mockito.ArgumentMatchers.eq("alice"))).thenThrow(failure);

        assertThatThrownBy(() -> readingSpaceService.getOverview("alice")).isSameAs(failure);

        verify(historyService).getHistory(org.mockito.ArgumentMatchers.any(ReadingHistoryPageQuery.class),
                org.mockito.ArgumentMatchers.eq("alice"));
        verifyNoMoreInteractions(historyService);
        verifyNoInteractions(favoriteService);
    }

    @Test
    void getOverview_shouldPropagateFavoriteFailureWithoutLastReadQuery() {
        Page<ReadingHistoryItem> history = new Page<>(1, 5);
        RuntimeException failure = new RuntimeException("favorites failed");
        when(historyService.getHistory(org.mockito.ArgumentMatchers.any(ReadingHistoryPageQuery.class),
                org.mockito.ArgumentMatchers.eq("alice"))).thenReturn(history);
        when(favoriteService.getMyFavorites(org.mockito.ArgumentMatchers.any(FavoritePageQuery.class),
                org.mockito.ArgumentMatchers.eq("alice"))).thenThrow(failure);

        assertThatThrownBy(() -> readingSpaceService.getOverview("alice")).isSameAs(failure);

        verify(historyService).getHistory(org.mockito.ArgumentMatchers.any(ReadingHistoryPageQuery.class),
                org.mockito.ArgumentMatchers.eq("alice"));
        verify(favoriteService).getMyFavorites(org.mockito.ArgumentMatchers.any(FavoritePageQuery.class),
                org.mockito.ArgumentMatchers.eq("alice"));
        verifyNoMoreInteractions(historyService, favoriteService);
    }

    @Test
    void getOverview_shouldPropagateLastReadFailure() {
        Page<ReadingHistoryItem> history = new Page<>(1, 5);
        Page<FavoriteArticleItem> favorites = new Page<>(1, 4);
        RuntimeException failure = new RuntimeException("last read failed");
        when(historyService.getHistory(org.mockito.ArgumentMatchers.any(ReadingHistoryPageQuery.class),
                org.mockito.ArgumentMatchers.eq("alice"))).thenReturn(history);
        when(favoriteService.getMyFavorites(org.mockito.ArgumentMatchers.any(FavoritePageQuery.class),
                org.mockito.ArgumentMatchers.eq("alice"))).thenReturn(favorites);
        when(historyService.getLastAvailable("alice")).thenThrow(failure);

        assertThatThrownBy(() -> readingSpaceService.getOverview("alice")).isSameAs(failure);

        verify(historyService).getHistory(org.mockito.ArgumentMatchers.any(ReadingHistoryPageQuery.class),
                org.mockito.ArgumentMatchers.eq("alice"));
        verify(favoriteService).getMyFavorites(org.mockito.ArgumentMatchers.any(FavoritePageQuery.class),
                org.mockito.ArgumentMatchers.eq("alice"));
        verify(historyService).getLastAvailable("alice");
        verifyNoMoreInteractions(historyService, favoriteService);
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
