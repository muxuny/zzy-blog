package com.blog.controller;

import com.blog.common.ArticleStatus;
import com.blog.common.Result;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import org.junit.jupiter.api.Test;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MyArticleControllerTest {

    private final ArticleService articleService = mock(ArticleService.class);
    private final MyArticleController myArticleController = new MyArticleController(articleService);

    @Test
    void submitUsesCurrentPrincipal() {
        Article article = new Article();
        article.setStatus(ArticleStatus.PENDING);
        Principal principal = () -> "alice";
        when(articleService.submitMyArticle(1L, "alice")).thenReturn(article);

        Result<Article> result = myArticleController.submit(1L, principal);

        assertEquals(200, result.getCode());
        assertEquals(ArticleStatus.PENDING, result.getData().getStatus());
        verify(articleService).submitMyArticle(1L, "alice");
    }
}
