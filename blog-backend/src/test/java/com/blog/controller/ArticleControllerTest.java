package com.blog.controller;

import com.blog.common.ArticleStatus;
import com.blog.common.BusinessException;
import com.blog.common.Result;
import com.blog.dto.ArticleNeighbors;
import com.blog.dto.ReadingPositionState;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import com.blog.service.ReadingHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ArticleControllerTest {

    private final ArticleService articleService = mock(ArticleService.class);

    private final ReadingHistoryService readingHistoryService = mock(ReadingHistoryService.class);

    private final ArticleController articleController = new ArticleController(articleService, readingHistoryService);

    @Test
    void detailReturnsPublicArticleDetail() {
        Article article = new Article();
        article.setStatus(ArticleStatus.PUBLISHED);
        when(articleService.getPublicDetail(1L)).thenReturn(article);

        Result<Article> result = articleController.detail(1L, null);

        assertEquals(200, result.getCode());
        assertEquals(ArticleStatus.PUBLISHED, result.getData().getStatus());
        verify(articleService).getPublicDetail(1L);
    }

    @Test
    void detailRecordsHistoryForAuthenticatedReader() {
        Article article = article(1L);
        Principal principal = () -> "alice";
        when(articleService.getPublicDetail(1L)).thenReturn(article);

        Result<Article> result = articleController.detail(1L, principal);

        assertEquals(200, result.getCode());
        assertEquals(article, result.getData());
        verify(readingHistoryService).record(article, "alice");
    }

    @Test
    void detailAttachesReadingPositionForAuthenticatedReader() {
        Article article = article(1L);
        ReadingPositionState state = new ReadingPositionState();
        state.setProgressPercent(42);
        Principal principal = () -> "alice";
        when(articleService.getPublicDetail(1L)).thenReturn(article);
        when(readingHistoryService.getPositionState(article, "alice")).thenReturn(state);

        Result<Article> result = articleController.detail(1L, principal);

        assertEquals(200, result.getCode());
        assertEquals(state, result.getData().getReadingPosition());
        verify(readingHistoryService).record(article, "alice");
        verify(readingHistoryService).getPositionState(article, "alice");
    }

    @Test
    void detailDoesNotRecordGuestHistory() {
        Article article = article(1L);
        when(articleService.getPublicDetail(1L)).thenReturn(article);

        Result<Article> result = articleController.detail(1L, null);

        assertEquals(200, result.getCode());
        assertEquals(article, result.getData());
        verifyNoInteractions(readingHistoryService);
    }

    @Test
    void detailStillReturnsArticleWhenHistoryWriteFails() {
        Article article = article(1L);
        Principal principal = () -> "alice";
        when(articleService.getPublicDetail(1L)).thenReturn(article);
        doThrow(new RuntimeException("history write failed"))
                .when(readingHistoryService).record(article, "alice");

        Result<Article> result = articleController.detail(1L, principal);

        assertEquals(200, result.getCode());
        assertEquals(article, result.getData());
        verify(readingHistoryService).record(article, "alice");
    }

    @Test
    void detailDoesNotRecordWhenPublicArticleLookupFails() {
        BusinessException exception = new BusinessException("article not found");
        Principal principal = () -> "alice";
        when(articleService.getPublicDetail(1L)).thenThrow(exception);

        BusinessException thrown = assertThrows(BusinessException.class,
                () -> articleController.detail(1L, principal));

        assertEquals(exception, thrown);
        verifyNoInteractions(readingHistoryService);
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
