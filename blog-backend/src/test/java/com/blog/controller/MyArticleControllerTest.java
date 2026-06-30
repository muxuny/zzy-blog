package com.blog.controller;

import com.blog.common.ArticleStatus;
import com.blog.common.Result;
import com.blog.dto.ArticleGroupAssignRequest;
import com.blog.dto.ArticleVisibilityRequest;
import com.blog.entity.Article;
import com.blog.entity.ArticleGroup;
import com.blog.service.ArticleService;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.Arrays;

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

    @Test
    void updateGroupsUsesCurrentPrincipal() {
        Article article = new Article();
        ArticleGroup group = new ArticleGroup();
        group.setId(10L);
        article.setGroups(Arrays.asList(group));
        Principal principal = () -> "alice";
        ArticleGroupAssignRequest request = new ArticleGroupAssignRequest();
        request.setGroupIds(Arrays.asList(10L));
        when(articleService.updateMyArticleGroups(1L, Arrays.asList(10L), "alice")).thenReturn(article);

        Result<Article> result = myArticleController.updateGroups(1L, request, principal);

        assertEquals(200, result.getCode());
        assertEquals(10L, result.getData().getGroups().get(0).getId());
        verify(articleService).updateMyArticleGroups(1L, Arrays.asList(10L), "alice");
    }

    @Test
    void updateVisibilityUsesCurrentPrincipal() {
        Article article = new Article();
        article.setVisibility("private");
        Principal principal = () -> "alice";
        ArticleVisibilityRequest request = new ArticleVisibilityRequest();
        request.setVisibility("private");
        when(articleService.updateMyArticleVisibility(1L, "private", "alice")).thenReturn(article);

        Result<Article> result = myArticleController.updateVisibility(1L, request, principal);

        assertEquals(200, result.getCode());
        assertEquals("private", result.getData().getVisibility());
        verify(articleService).updateMyArticleVisibility(1L, "private", "alice");
    }
}
