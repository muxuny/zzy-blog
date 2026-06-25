package com.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.ArticleNeighbors;
import com.blog.dto.ArticlePageQuery;
import com.blog.dto.ArticleRequest;
import com.blog.entity.Article;

import java.util.List;

public interface ArticleService extends IService<Article> {
    IPage<Article> getPublicPage(ArticlePageQuery query);
    Article getPublicDetail(Long id);
    ArticleNeighbors getPublicNeighbors(Long id);
    List<Article> getRelatedArticles(Long id, Integer size);

    IPage<Article> getMyPage(ArticlePageQuery query, String username);
    Article getMyDetail(Long id, String username);
    Article createMyArticle(ArticleRequest request, String username);
    Article updateMyArticle(Long id, ArticleRequest request, String username);
    Article updateMyArticleGroups(Long id, List<Long> groupIds, String username);
    void deleteMyArticle(Long id, String username);
    Article submitMyArticle(Long id, String username);
    Article withdrawMyArticle(Long id, String username);

    Article getAdminDetail(Long id);
    Article createAdminArticle(ArticleRequest request);
    Article updateAdminArticle(Long id, ArticleRequest request);
    void deleteAdminArticle(Long id);
    Article approveArticle(Long id);
    Article rejectArticle(Long id, String reason);

    IPage<Article> getAdminPage(ArticlePageQuery query);
}
