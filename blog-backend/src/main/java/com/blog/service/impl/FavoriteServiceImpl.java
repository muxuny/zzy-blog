package com.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.BusinessException;
import com.blog.dto.FavoriteArticleItem;
import com.blog.dto.FavoritePageQuery;
import com.blog.dto.FavoriteRelationRow;
import com.blog.dto.FavoriteStatus;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.mapper.ArticleFavoriteMapper;
import com.blog.service.ArticleService;
import com.blog.service.FavoriteService;
import com.blog.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private static final long MAX_PAGE_SIZE = 100;

    private final ArticleFavoriteMapper favoriteMapper;
    private final ArticleService articleService;
    private final UserService userService;

    public FavoriteServiceImpl(ArticleFavoriteMapper favoriteMapper,
                               ArticleService articleService,
                               UserService userService) {
        this.favoriteMapper = favoriteMapper;
        this.articleService = articleService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public void favoriteArticle(Long articleId, String username) {
        User user = requireUser(username);
        Article article;
        try {
            article = articleService.getPublicArticleSummary(articleId);
        } catch (BusinessException exception) {
            throw new BusinessException("文章不存在或暂不可收藏");
        }
        favoriteMapper.upsertFavorite(
                IdWorker.getId(), user.getId(), articleId, article.getTitle(),
                username, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void unfavoriteArticle(Long articleId, String username) {
        User user = requireUser(username);
        favoriteMapper.cancelFavorite(user.getId(), articleId, username, LocalDateTime.now());
    }

    @Override
    public FavoriteStatus getFavoriteStatus(Long articleId, String username) {
        User user = requireUser(username);
        return new FavoriteStatus(favoriteMapper.countActiveFavorite(user.getId(), articleId) > 0);
    }

    @Override
    public IPage<FavoriteArticleItem> getMyFavorites(FavoritePageQuery query, String username) {
        validateFavoritePage(query);
        User user = requireUser(username);
        return getFavoritesForUser(query, user.getId());
    }

    @Override
    public IPage<FavoriteArticleItem> getMyFavoritesForUser(FavoritePageQuery query, Long userId) {
        validateFavoritePage(query);
        return getFavoritesForUser(query, userId);
    }

    private IPage<FavoriteArticleItem> getFavoritesForUser(FavoritePageQuery query, Long userId) {
        Page<FavoriteRelationRow> requestPage = new Page<>(query.getPage(), query.getSize());
        IPage<FavoriteRelationRow> relationPage = favoriteMapper.selectFavoritePage(
                requestPage, userId, query.getKeyword(), query.getTagId());

        List<Long> availableIds = relationPage.getRecords().stream()
                .filter(FavoriteRelationRow::isAvailable)
                .map(FavoriteRelationRow::getArticleId)
                .collect(Collectors.toList());
        Map<Long, Article> articlesById = articleService.getPublicArticleSummaries(availableIds).stream()
                .collect(Collectors.toMap(Article::getId, Function.identity()));
        List<FavoriteArticleItem> items = relationPage.getRecords().stream()
                .map(row -> toFavoriteItem(row, articlesById.get(row.getArticleId())))
                .collect(Collectors.toList());

        Page<FavoriteArticleItem> result = new Page<>(
                relationPage.getCurrent(), relationPage.getSize(), relationPage.getTotal());
        result.setRecords(items);
        return result;
    }

    private FavoriteArticleItem toFavoriteItem(FavoriteRelationRow row, Article article) {
        FavoriteArticleItem item = new FavoriteArticleItem();
        item.setArticleId(row.getArticleId());
        item.setFavoritedAt(row.getFavoritedAt());
        if (!row.isAvailable() || article == null) {
            item.setTitle(row.getTitleSnapshot());
            item.setAvailable(false);
            item.setTags(Collections.emptyList());
            item.setUnavailableMessage("该文章暂未公开");
            return item;
        }
        item.setTitle(article.getTitle());
        item.setSummary(article.getSummary());
        item.setCoverImage(article.getCoverImage());
        item.setAuthorName(article.getAuthorName());
        item.setTags(article.getTags() == null ? Collections.emptyList() : article.getTags());
        item.setViewCount(article.getViewCount());
        item.setAvailable(true);
        return item;
    }

    private void validateFavoritePage(FavoritePageQuery query) {
        if (query == null || query.getPage() < 1
                || query.getSize() < 1 || query.getSize() > MAX_PAGE_SIZE) {
            throw new BusinessException("分页参数不合法");
        }
    }

    private User requireUser(String username) {
        User user = userService.getCurrentUser(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
}
