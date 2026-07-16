package com.blog.controller;

import com.blog.common.ArticleStatus;
import com.blog.common.Result;
import com.blog.dto.ArticleNeighbors;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    void neighborsReturnsPublicArticleNeighbors() {
        Article previous = article(1L);
        Article next = article(3L);
        ArticleNeighbors neighbors = new ArticleNeighbors(previous, next);
        when(articleService.getPublicNeighbors(2L)).thenReturn(neighbors);

        Result<ArticleNeighbors> result = articleController.neighbors(2L);

        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData().getPrevious().getId());
        assertEquals(3L, result.getData().getNext().getId());
        verify(articleService).getPublicNeighbors(2L);
    }

    @Test
    void relatedReturnsRelatedPublicArticlesWithDefaultSize() throws Exception {
        Article related = article(2L);
        when(articleService.getRelatedArticles(1L, 4)).thenReturn(Collections.singletonList(related));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(articleController).build();

        mockMvc.perform(get("/api/articles/1/related"))
                .andExpect(status().isOk());

        verify(articleService).getRelatedArticles(1L, 4);
    }

    private static Article article(Long id) {
        Article article = new Article();
        article.setId(id);
        article.setStatus(ArticleStatus.PUBLISHED);
        return article;
    }
}
