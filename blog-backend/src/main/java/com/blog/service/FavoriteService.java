package com.blog.service;

import com.blog.dto.FavoriteStatus;

public interface FavoriteService {

    void favoriteArticle(Long articleId, String username);

    void unfavoriteArticle(Long articleId, String username);

    FavoriteStatus getFavoriteStatus(Long articleId, String username);
}
