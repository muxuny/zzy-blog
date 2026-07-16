package com.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.ArticleNeighbors;
import com.blog.dto.ArticlePageQuery;
import com.blog.dto.ArticleRequest;
import com.blog.entity.Article;

import java.util.List;

/**
 * 文章领域服务，按公开阅读、创作者和后台管理三个使用场景组织接口。
 */
public interface ArticleService extends IService<Article> {

    // 公开阅读侧
    IPage<Article> getPublicPage(ArticlePageQuery query);

    Article getPublicDetail(Long id);

    ArticleNeighbors getPublicNeighbors(Long id);

    List<Article> getRelatedArticles(Long id, Integer size);

    // 创作者侧
    IPage<Article> getMyPage(ArticlePageQuery query, String username);

    Article getMyDetail(Long id, String username);

    Article createMyArticle(ArticleRequest request, String username);

    Article updateMyArticle(Long id, ArticleRequest request, String username);

    Article updateMyArticleGroups(Long id, List<Long> groupIds, String username);

    Article updateMyArticleVisibility(Long id, String visibility, String username);

    void deleteMyArticle(Long id, String username);

    Article submitMyArticle(Long id, String username);

    Article withdrawMyArticle(Long id, String username);

    // 后台管理侧
    Article getAdminDetail(Long id);

    Article createAdminArticle(ArticleRequest request);

    Article updateAdminArticle(Long id, ArticleRequest request);

    void deleteAdminArticle(Long id);

    Article approveArticle(Long id);

    Article rejectArticle(Long id, String reason);

    IPage<Article> getAdminPage(ArticlePageQuery query);
}
