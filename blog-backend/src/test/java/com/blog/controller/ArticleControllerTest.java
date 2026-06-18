package com.blog.controller;

import com.blog.common.ArticleStatus;
import com.blog.common.Result;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArticleControllerTest {

    private final ArticleService articleService = mock(ArticleService.class);
    private final ArticleController articleController = new ArticleController(articleService);

    @Test
    void detailReturnsPublicArticleDetail() {
        Article article = new Article();
        article.setStatus(ArticleStatus.PUBLISHED);
        when(articleService.getPublicDetail(1L)).thenReturn(article);

        Result<Article> result = articleController.detail(1L);

        assertEquals(200, result.getCode());
        assertEquals(ArticleStatus.PUBLISHED, result.getData().getStatus());
        verify(articleService).getPublicDetail(1L);
    }
}
