package com.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.BusinessException;
import com.blog.dto.ReadingHistoryItem;
import com.blog.dto.ReadingHistoryOverview;
import com.blog.dto.ReadingHistoryPageQuery;
import com.blog.dto.ReadingHistoryRelationRow;
import com.blog.dto.ReadingPositionRequest;
import com.blog.dto.ReadingPositionState;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.mapper.ArticleReadingHistoryMapper;
import com.blog.service.ArticleService;
import com.blog.service.ReadingHistoryService;
import com.blog.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReadingHistoryServiceImpl implements ReadingHistoryService {

    private static final long MAX_PAGE_SIZE = 100;
    private static final String UNAVAILABLE_MESSAGE = "该文章暂未公开";
    private static final int MIN_RESUME_PROGRESS = 5;
    private static final int MAX_PROGRESS = 100;
    private static final int MAX_ANCHOR_ID_LENGTH = 160;

    private final ArticleReadingHistoryMapper historyMapper;
    private final ArticleService articleService;
    private final UserService userService;

    public ReadingHistoryServiceImpl(ArticleReadingHistoryMapper historyMapper,
                                     ArticleService articleService,
                                     UserService userService) {
        this.historyMapper = historyMapper;
        this.articleService = articleService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public void record(Article article, String username) {
        User user = requireUser(username);
        LocalDateTime now = LocalDateTime.now();
        historyMapper.upsertHistory(IdWorker.getId(), user.getId(), article.getId(), article.getTitle(),
                username, now);
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<ReadingHistoryItem> getHistory(ReadingHistoryPageQuery query, String username) {
        validatePageQuery(query);
        User user = requireUser(username);
        Page<ReadingHistoryRelationRow> requestPage = new Page<>(query.getPage(), query.getSize());
        IPage<ReadingHistoryRelationRow> relationPage = historyMapper.selectHistoryPage(requestPage,
                user.getId());

        List<Long> availableIds = relationPage.getRecords().stream()
                .filter(ReadingHistoryRelationRow::isAvailable)
                .map(ReadingHistoryRelationRow::getArticleId)
                .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new),
                        java.util.ArrayList::new));
        Map<Long, Article> articlesById = getPublicArticlesById(availableIds);
        List<ReadingHistoryItem> items = relationPage.getRecords().stream()
                .map(row -> toReadingHistoryItem(row, articlesById.get(row.getArticleId())))
                .collect(Collectors.toList());

        Page<ReadingHistoryItem> result = new Page<>(relationPage.getCurrent(), relationPage.getSize(),
                relationPage.getTotal());
        result.setRecords(items);
        return result;
    }

    @Override
    @Transactional
    public void savePosition(Long articleId, ReadingPositionRequest request, String username) {
        validatePositionRequest(request);
        User user = requireUser(username);
        LocalDateTime now = LocalDateTime.now();
        int updated = historyMapper.updatePosition(user.getId(), articleId,
                request.getProgressPercent(), request.getScrollY(),
                normalizeAnchorId(request.getAnchorId()), request.getAnchorOffset(),
                request.getArticleUpdatedAt(), username, now);
        if (updated == 0) {
            throw new BusinessException("阅读历史不存在或文章暂不可用");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReadingPositionState getPositionState(Article article, String username) {
        User user = requireUser(username);
        ReadingHistoryRelationRow row = historyMapper.selectActiveHistory(user.getId(), article.getId());
        return row == null ? emptyPositionState() : toPositionState(row);
    }

    @Override
    public ReadingHistoryOverview getOverview(long recentSize, Long userId) {
        validateRecentSize(recentSize);
        Page<ReadingHistoryRelationRow> requestPage = new Page<>(1, recentSize);
        IPage<ReadingHistoryRelationRow> relationPage = historyMapper.selectHistoryPage(requestPage, userId);
        ReadingHistoryRelationRow lastRow = historyMapper.selectLastAvailable(userId);

        LinkedHashSet<Long> availableIds = relationPage.getRecords().stream()
                .filter(ReadingHistoryRelationRow::isAvailable)
                .map(ReadingHistoryRelationRow::getArticleId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (lastRow != null && lastRow.isAvailable()) {
            availableIds.add(lastRow.getArticleId());
        }
        Map<Long, Article> articlesById = getPublicArticlesById(new java.util.ArrayList<>(availableIds));
        List<ReadingHistoryItem> items = relationPage.getRecords().stream()
                .map(row -> toReadingHistoryItem(row, articlesById.get(row.getArticleId())))
                .collect(Collectors.toList());
        Page<ReadingHistoryItem> recentHistory = new Page<>(relationPage.getCurrent(),
                relationPage.getSize(), relationPage.getTotal());
        recentHistory.setRecords(items);

        ReadingHistoryOverview overview = new ReadingHistoryOverview();
        overview.setRecentHistory(recentHistory);
        overview.setLastRead(lastRow == null ? null
                : articlesById.containsKey(lastRow.getArticleId())
                ? toReadingHistoryItem(lastRow, articlesById.get(lastRow.getArticleId())) : null);
        return overview;
    }

    @Override
    public ReadingHistoryItem getLastAvailable(String username) {
        User user = requireUser(username);
        ReadingHistoryRelationRow row = historyMapper.selectLastAvailable(user.getId());
        if (row == null) {
            return null;
        }
        List<Article> articles = articleService.getPublicArticleSummaries(
                Collections.singletonList(row.getArticleId()));
        if (articles == null || articles.isEmpty()) {
            return null;
        }
        Article article = articles.stream()
                .filter(candidate -> row.getArticleId().equals(candidate.getId()))
                .findFirst()
                .orElse(null);
        return article == null ? null : toReadingHistoryItem(row, article);
    }

    @Override
    @Transactional
    public void deleteHistory(Long articleId, String username) {
        User user = requireUser(username);
        historyMapper.deleteHistory(user.getId(), articleId, username, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void clearHistory(String username) {
        User user = requireUser(username);
        historyMapper.clearHistory(user.getId(), username, LocalDateTime.now());
    }

    private Map<Long, Article> getPublicArticlesById(List<Long> articleIds) {
        if (articleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Article> articles = articleService.getPublicArticleSummaries(articleIds);
        if (articles == null) {
            return Collections.emptyMap();
        }
        return articles.stream().collect(Collectors.toMap(Article::getId, Function.identity()));
    }

    private ReadingHistoryItem toReadingHistoryItem(ReadingHistoryRelationRow row, Article article) {
        ReadingHistoryItem item = new ReadingHistoryItem();
        item.setArticleId(row.getArticleId());
        item.setFirstReadAt(row.getFirstReadAt());
        item.setLastReadAt(row.getLastReadAt());
        item.setReadCount(row.getReadCount());
        ReadingPositionState position = toPositionState(row);
        item.setProgressPercent(position.getProgressPercent());
        item.setPositionUpdatedAt(position.getPositionUpdatedAt());
        item.setCanResume(position.isCanResume());
        item.setResumeScrollY(position.getResumeScrollY());
        item.setResumeAnchorId(position.getResumeAnchorId());
        item.setResumeAnchorOffset(position.getResumeAnchorOffset());
        if (!row.isAvailable() || article == null) {
            item.setTitle(row.getTitleSnapshot());
            item.setAvailable(false);
            item.setUnavailableMessage(UNAVAILABLE_MESSAGE);
            return item;
        }
        item.setTitle(article.getTitle());
        item.setSummary(article.getSummary());
        item.setCoverImage(article.getCoverImage());
        item.setAuthorName(article.getAuthorName());
        item.setViewCount(article.getViewCount());
        item.setAvailable(true);
        return item;
    }

    private void validatePageQuery(ReadingHistoryPageQuery query) {
        if (query == null || query.getPage() < 1 || query.getSize() < 1
                || query.getSize() > MAX_PAGE_SIZE) {
            throw new BusinessException("分页参数不合法");
        }
    }

    private void validateRecentSize(long recentSize) {
        if (recentSize < 1 || recentSize > MAX_PAGE_SIZE) {
            throw new BusinessException("分页参数不合法");
        }
    }

    private void validatePositionRequest(ReadingPositionRequest request) {
        if (request == null || request.getProgressPercent() == null || request.getScrollY() == null
                || request.getArticleUpdatedAt() == null
                || request.getProgressPercent() < 0 || request.getProgressPercent() > MAX_PROGRESS
                || request.getScrollY() < 0
                || request.getAnchorOffset() != null && request.getAnchorOffset() < 0) {
            throw new BusinessException("阅读位置参数不合法");
        }
        String anchorId = request.getAnchorId();
        if (anchorId != null && anchorId.trim().length() > MAX_ANCHOR_ID_LENGTH) {
            throw new BusinessException("阅读位置参数不合法");
        }
    }

    private String normalizeAnchorId(String anchorId) {
        if (anchorId == null) {
            return null;
        }
        String trimmed = anchorId.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ReadingPositionState emptyPositionState() {
        ReadingPositionState state = new ReadingPositionState();
        state.setProgressPercent(0);
        state.setCanResume(false);
        return state;
    }

    private ReadingPositionState toPositionState(ReadingHistoryRelationRow row) {
        ReadingPositionState state = new ReadingPositionState();
        int progress = normalizeProgress(row.getProgressPercent());
        state.setProgressPercent(progress);
        state.setPositionUpdatedAt(row.getPositionUpdatedAt());
        boolean canResume = row.isAvailable()
                && progress >= MIN_RESUME_PROGRESS
                && row.getArticleUpdatedAtSnapshot() != null
                && row.getArticleUpdatedAtSnapshot().equals(row.getArticleUpdatedAt());
        state.setCanResume(canResume);
        if (canResume) {
            state.setResumeScrollY(row.getScrollY());
            state.setResumeAnchorId(row.getAnchorId());
            state.setResumeAnchorOffset(row.getAnchorOffset());
        }
        return state;
    }

    private int normalizeProgress(Integer progressPercent) {
        if (progressPercent == null) {
            return 0;
        }
        return Math.max(0, Math.min(MAX_PROGRESS, progressPercent));
    }

    private User requireUser(String username) {
        User user = userService.getCurrentUser(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
}
