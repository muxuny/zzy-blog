package com.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.BusinessException;
import com.blog.dto.ReadingHistoryItem;
import com.blog.dto.ReadingHistoryPageQuery;
import com.blog.dto.ReadingHistoryRelationRow;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.mapper.ArticleReadingHistoryMapper;
import com.blog.service.impl.ReadingHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

class ReadingHistoryServiceImplTest {

    private ArticleReadingHistoryMapper historyMapper;
    private ArticleService articleService;
    private UserService userService;
    private ReadingHistoryService readingHistoryService;

    @BeforeEach
    void setUp() {
        historyMapper = mock(ArticleReadingHistoryMapper.class);
        articleService = mock(ArticleService.class);
        userService = mock(UserService.class);
        readingHistoryService = new ReadingHistoryServiceImpl(historyMapper, articleService, userService);
    }

    @Test
    void record_shouldSaveCurrentUserAndArticleTitleWithCurrentTime() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        Article article = article(20L, "Saved title", null, null, null, null);
        LocalDateTime before = LocalDateTime.now();

        readingHistoryService.record(article, "alice");

        LocalDateTime after = LocalDateTime.now();
        ArgumentCaptor<LocalDateTime> nowCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(userService).getCurrentUser("alice");
        verify(historyMapper).upsertHistory(anyLong(), eq(10L), eq(20L), eq("Saved title"),
                eq("alice"), nowCaptor.capture());
        assertThat(nowCaptor.getValue()).isBetween(before, after);
        verifyNoInteractions(articleService);
        verifyNoMoreInteractions(userService, historyMapper);
    }

    @Test
    void record_shouldRejectMissingUserBeforeWritingHistory() {
        when(userService.getCurrentUser("missing")).thenReturn(null);
        Article article = article(20L, "Saved title", null, null, null, null);

        assertThatThrownBy(() -> readingHistoryService.record(article, "missing"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");

        verify(userService).getCurrentUser("missing");
        verifyNoInteractions(historyMapper, articleService);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getHistory_shouldUseLivePublicDetailsAndUnavailableSnapshotsInOnePage() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        LocalDateTime firstReadAt = LocalDateTime.of(2026, 7, 19, 9, 0);
        LocalDateTime lastReadAt = LocalDateTime.of(2026, 7, 20, 10, 30);
        ReadingHistoryRelationRow available = relation(1L, 20L, "Old public title",
                firstReadAt, lastReadAt, 3, true);
        ReadingHistoryRelationRow unavailable = relation(2L, 21L, "Saved private title",
                firstReadAt.minusDays(1), lastReadAt.minusDays(1), 2, false);
        Page<ReadingHistoryRelationRow> rows = new Page<>(2, 5);
        rows.setTotal(8);
        rows.setRecords(Arrays.asList(available, unavailable));
        when(historyMapper.selectHistoryPage(any(Page.class), eq(10L))).thenReturn(rows);
        Article current = article(20L, "Current public title", "Public summary", "cover.png",
                "Current author", 42);
        when(articleService.getPublicArticleSummaries(Collections.singletonList(20L)))
                .thenReturn(Collections.singletonList(current));
        ReadingHistoryPageQuery query = query(2, 5);

        IPage<ReadingHistoryItem> result = readingHistoryService.getHistory(query, "alice");

        assertThat(result.getCurrent()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getTotal()).isEqualTo(8);
        ReadingHistoryItem visible = result.getRecords().get(0);
        assertThat(visible.getArticleId()).isEqualTo(20L);
        assertThat(visible.getTitle()).isEqualTo("Current public title");
        assertThat(visible.getSummary()).isEqualTo("Public summary");
        assertThat(visible.getCoverImage()).isEqualTo("cover.png");
        assertThat(visible.getAuthorName()).isEqualTo("Current author");
        assertThat(visible.getViewCount()).isEqualTo(42);
        assertThat(visible.getFirstReadAt()).isEqualTo(firstReadAt);
        assertThat(visible.getLastReadAt()).isEqualTo(lastReadAt);
        assertThat(visible.getReadCount()).isEqualTo(3);
        assertThat(visible.isAvailable()).isTrue();
        assertThat(visible.getUnavailableMessage()).isNull();

        ReadingHistoryItem hidden = result.getRecords().get(1);
        assertThat(hidden.getArticleId()).isEqualTo(21L);
        assertThat(hidden.getTitle()).isEqualTo("Saved private title");
        assertThat(hidden.isAvailable()).isFalse();
        assertThat(hidden.getFirstReadAt()).isEqualTo(firstReadAt.minusDays(1));
        assertThat(hidden.getLastReadAt()).isEqualTo(lastReadAt.minusDays(1));
        assertThat(hidden.getReadCount()).isEqualTo(2);
        assertThat(Arrays.asList(hidden.getSummary(), hidden.getCoverImage(),
                hidden.getAuthorName(), hidden.getViewCount())).containsOnlyNulls();
        assertThat(hidden.getUnavailableMessage()).isEqualTo("该文章暂未公开");
        ArgumentCaptor<Page<ReadingHistoryRelationRow>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(historyMapper).selectHistoryPage(pageCaptor.capture(), eq(10L));
        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(2);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(5);
        verify(articleService).getPublicArticleSummaries(Collections.singletonList(20L));
    }

    @Test
    void getHistory_shouldSafelyFallbackWhenAvailableArticleDisappearsFromBatch() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        ReadingHistoryRelationRow available = relation(1L, 20L, "Saved public title",
                LocalDateTime.of(2026, 7, 19, 9, 0), LocalDateTime.of(2026, 7, 20, 10, 30),
                3, true);
        Page<ReadingHistoryRelationRow> rows = new Page<>(1, 10);
        rows.setTotal(1);
        rows.setRecords(Collections.singletonList(available));
        when(historyMapper.selectHistoryPage(any(Page.class), eq(10L))).thenReturn(rows);
        when(articleService.getPublicArticleSummaries(Collections.singletonList(20L)))
                .thenReturn(Collections.emptyList());

        IPage<ReadingHistoryItem> result = readingHistoryService.getHistory(query(1, 10), "alice");

        ReadingHistoryItem hidden = result.getRecords().get(0);
        assertThat(hidden.getTitle()).isEqualTo("Saved public title");
        assertThat(hidden.isAvailable()).isFalse();
        assertThat(hidden.getUnavailableMessage()).isEqualTo("该文章暂未公开");
        assertThat(Arrays.asList(hidden.getSummary(), hidden.getCoverImage(),
                hidden.getAuthorName(), hidden.getViewCount())).containsOnlyNulls();
    }

    @Test
    void getHistory_shouldNotLookupArticlesWhenThePageHasNoAvailableRows() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        Page<ReadingHistoryRelationRow> rows = new Page<>(1, 10);
        rows.setTotal(1);
        rows.setRecords(Collections.singletonList(relation(1L, 20L, "Saved unavailable title",
                LocalDateTime.of(2026, 7, 19, 9, 0), LocalDateTime.of(2026, 7, 20, 10, 30),
                1, false)));
        when(historyMapper.selectHistoryPage(any(Page.class), eq(10L))).thenReturn(rows);

        IPage<ReadingHistoryItem> result = readingHistoryService.getHistory(query(1, 10), "alice");

        assertThat(result.getRecords()).singleElement().satisfies(item -> {
            assertThat(item.isAvailable()).isFalse();
            assertThat(item.getTitle()).isEqualTo("Saved unavailable title");
            assertThat(item.getSummary()).isNull();
            assertThat(item.getCoverImage()).isNull();
            assertThat(item.getAuthorName()).isNull();
            assertThat(item.getViewCount()).isNull();
        });
        verifyNoInteractions(articleService);
    }

    @Test
    void getHistory_shouldBatchDistinctAvailableArticleIdsOnce() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        Page<ReadingHistoryRelationRow> rows = new Page<>(1, 10);
        rows.setTotal(2);
        rows.setRecords(Arrays.asList(
                relation(1L, 20L, "First saved title", LocalDateTime.of(2026, 7, 19, 9, 0),
                        LocalDateTime.of(2026, 7, 20, 10, 30), 3, true),
                relation(2L, 20L, "Second saved title", LocalDateTime.of(2026, 7, 18, 9, 0),
                        LocalDateTime.of(2026, 7, 19, 10, 30), 1, true)));
        when(historyMapper.selectHistoryPage(any(Page.class), eq(10L))).thenReturn(rows);
        when(articleService.getPublicArticleSummaries(Collections.singletonList(20L)))
                .thenReturn(Collections.singletonList(article(20L, "Current title", "Summary", "cover.png",
                        "Author", 42)));

        readingHistoryService.getHistory(query(1, 10), "alice");

        verify(articleService).getPublicArticleSummaries(Collections.singletonList(20L));
    }

    @Test
    void getLastAvailable_shouldReturnNullWithoutArticleLookupWhenThereIsNoHistoryRow() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        when(historyMapper.selectLastAvailable(10L)).thenReturn(null);

        assertThat(readingHistoryService.getLastAvailable("alice")).isNull();

        verify(historyMapper).selectLastAvailable(10L);
        verifyNoInteractions(articleService);
    }

    @Test
    void getLastAvailable_shouldReturnLivePublicArticleWithReadMetadata() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        LocalDateTime firstReadAt = LocalDateTime.of(2026, 7, 19, 9, 0);
        LocalDateTime lastReadAt = LocalDateTime.of(2026, 7, 20, 10, 30);
        when(historyMapper.selectLastAvailable(10L)).thenReturn(relation(1L, 20L, "Saved title",
                firstReadAt, lastReadAt, 3, true));
        when(articleService.getPublicArticleSummaries(Collections.singletonList(20L)))
                .thenReturn(Collections.singletonList(article(20L, "Current title", "Summary", "cover.png",
                        "Author", 42)));

        ReadingHistoryItem result = readingHistoryService.getLastAvailable("alice");

        assertThat(result.getArticleId()).isEqualTo(20L);
        assertThat(result.getTitle()).isEqualTo("Current title");
        assertThat(result.getSummary()).isEqualTo("Summary");
        assertThat(result.getCoverImage()).isEqualTo("cover.png");
        assertThat(result.getAuthorName()).isEqualTo("Author");
        assertThat(result.getViewCount()).isEqualTo(42);
        assertThat(result.getFirstReadAt()).isEqualTo(firstReadAt);
        assertThat(result.getLastReadAt()).isEqualTo(lastReadAt);
        assertThat(result.getReadCount()).isEqualTo(3);
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getUnavailableMessage()).isNull();
    }

    @Test
    void getLastAvailable_shouldReturnNullWhenPublicSummaryDisappears() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        when(historyMapper.selectLastAvailable(10L)).thenReturn(relation(1L, 20L, "Saved title",
                LocalDateTime.of(2026, 7, 19, 9, 0), LocalDateTime.of(2026, 7, 20, 10, 30), 3, true));
        when(articleService.getPublicArticleSummaries(Collections.singletonList(20L)))
                .thenReturn(Collections.emptyList());

        assertThat(readingHistoryService.getLastAvailable("alice")).isNull();
    }

    @Test
    void getHistory_shouldRejectInvalidPagingBeforeUsingDependencies() {
        assertInvalidPage(null);
        assertInvalidPage(query(0, 10));
        assertInvalidPage(query(1, 0));
        assertInvalidPage(query(1, 101));
    }

    @Test
    void deleteHistory_shouldUseOnlyCurrentUserAndBeIdempotent() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        LocalDateTime before = LocalDateTime.now();

        readingHistoryService.deleteHistory(20L, "alice");

        LocalDateTime after = LocalDateTime.now();
        ArgumentCaptor<LocalDateTime> nowCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(historyMapper).deleteHistory(eq(10L), eq(20L), eq("alice"), nowCaptor.capture());
        assertThat(nowCaptor.getValue()).isBetween(before, after);
        verifyNoInteractions(articleService);
    }

    @Test
    void clearHistory_shouldUseOnlyCurrentUser() {
        when(userService.getCurrentUser("alice")).thenReturn(user(10L, "alice"));
        LocalDateTime before = LocalDateTime.now();

        readingHistoryService.clearHistory("alice");

        LocalDateTime after = LocalDateTime.now();
        ArgumentCaptor<LocalDateTime> nowCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(historyMapper).clearHistory(eq(10L), eq("alice"), nowCaptor.capture());
        assertThat(nowCaptor.getValue()).isBetween(before, after);
        verifyNoInteractions(articleService);
    }

    @Test
    void deleteAndClearHistory_shouldRejectMissingUserBeforeWriting() {
        when(userService.getCurrentUser("missing")).thenReturn(null);

        assertThatThrownBy(() -> readingHistoryService.deleteHistory(20L, "missing"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");
        assertThatThrownBy(() -> readingHistoryService.clearHistory("missing"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");

        verify(userService, org.mockito.Mockito.times(2)).getCurrentUser("missing");
        verifyNoInteractions(historyMapper, articleService);
    }

    @Test
    void readingHistoryItem_shouldExposeOnlyTheHistoryPresentationFields() {
        List<String> fieldNames = Arrays.asList(ReadingHistoryItem.class.getDeclaredFields()).stream()
                .map(field -> field.getName())
                .collect(java.util.stream.Collectors.toList());

        assertThat(fieldNames).containsExactlyInAnyOrder(
                "articleId", "title", "summary", "coverImage", "authorName", "viewCount",
                "firstReadAt", "lastReadAt", "readCount", "available", "unavailableMessage");
        assertThat(ReadingHistoryItem.class.getDeclaredFields())
                .filteredOn(field -> field.getName().equals("articleId"))
                .singleElement()
                .extracting(field -> field.getType())
                .isEqualTo(Long.class);
        assertThat(ReadingHistoryItem.class.getDeclaredFields())
                .filteredOn(field -> field.getName().equals("firstReadAt"))
                .singleElement()
                .extracting(field -> field.getType())
                .isEqualTo(LocalDateTime.class);
        assertThat(ReadingHistoryItem.class.getDeclaredFields())
                .filteredOn(field -> field.getName().equals("readCount"))
                .singleElement()
                .extracting(field -> field.getType())
                .isEqualTo(Integer.class);
    }

    private void assertInvalidPage(ReadingHistoryPageQuery query) {
        assertThatThrownBy(() -> readingHistoryService.getHistory(query, "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分页参数不合法");
        verifyNoInteractions(userService, historyMapper, articleService);
    }

    private static ReadingHistoryPageQuery query(long page, long size) {
        ReadingHistoryPageQuery query = new ReadingHistoryPageQuery();
        query.setPage(page);
        query.setSize(size);
        return query;
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private static ReadingHistoryRelationRow relation(Long historyId, Long articleId,
                                                       String titleSnapshot, LocalDateTime firstReadAt,
                                                       LocalDateTime lastReadAt, Integer readCount,
                                                       boolean available) {
        ReadingHistoryRelationRow row = new ReadingHistoryRelationRow();
        row.setHistoryId(historyId);
        row.setArticleId(articleId);
        row.setTitleSnapshot(titleSnapshot);
        row.setFirstReadAt(firstReadAt);
        row.setLastReadAt(lastReadAt);
        row.setReadCount(readCount);
        row.setAvailable(available);
        return row;
    }

    private static Article article(Long id, String title, String summary, String coverImage,
                                   String authorName, Integer viewCount) {
        Article article = new Article();
        article.setId(id);
        article.setTitle(title);
        article.setSummary(summary);
        article.setCoverImage(coverImage);
        article.setAuthorName(authorName);
        article.setViewCount(viewCount);
        return article;
    }
}
