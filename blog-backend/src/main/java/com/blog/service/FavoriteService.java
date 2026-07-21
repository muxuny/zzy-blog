package com.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blog.dto.FavoriteArticleItem;
import com.blog.dto.FavoritePageQuery;
import com.blog.dto.FavoriteStatus;

public interface FavoriteService {

    void favoriteArticle(Long articleId, String username);

    void unfavoriteArticle(Long articleId, String username);

    FavoriteStatus getFavoriteStatus(Long articleId, String username);

    IPage<FavoriteArticleItem> getMyFavorites(FavoritePageQuery query, String username);

    IPage<FavoriteArticleItem> getMyFavoritesForUser(FavoritePageQuery query, Long userId);
}
