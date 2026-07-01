package com.blog.controller.admin;

import com.blog.common.ArticleStatus;
import com.blog.common.Result;
import com.blog.dto.ArticleReviewRequest;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminArticleControllerTest {

    private final ArticleService articleService = mock(ArticleService.class);

    private final AdminArticleController adminArticleController = new AdminArticleController(articleService);

    @Test
    void rejectPassesReasonToService() {
        ArticleReviewRequest request = new ArticleReviewRequest();
        request.setReason("内容不完整");
        Article article = new Article();
        article.setStatus(ArticleStatus.REJECTED);
        article.setReviewReason("内容不完整");
        when(articleService.rejectArticle(1L, "内容不完整")).thenReturn(article);

        Result<Article> result = adminArticleController.reject(1L, request);

        assertEquals(200, result.getCode());
        assertEquals(ArticleStatus.REJECTED, result.getData().getStatus());
        assertEquals("内容不完整", result.getData().getReviewReason());
        verify(articleService).rejectArticle(1L, "内容不完整");
    }
}
