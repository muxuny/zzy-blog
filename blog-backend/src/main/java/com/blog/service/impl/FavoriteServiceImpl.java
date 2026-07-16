package com.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.blog.common.BusinessException;
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

@Service
public class FavoriteServiceImpl implements FavoriteService {

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

    private User requireUser(String username) {
        User user = userService.getCurrentUser(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
}
