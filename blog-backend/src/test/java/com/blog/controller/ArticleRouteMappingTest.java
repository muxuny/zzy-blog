package com.blog.controller;

import com.blog.common.ArticleStatus;
import com.blog.controller.admin.AdminArticleController;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ArticleRouteMappingTest {

    private MockMvc mockMvc;
    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        articleService = mock(ArticleService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new MyArticleController(articleService),
                new AdminArticleController(articleService))
                .build();
    }

    @Test
    void putSubmitRouteIsMapped() throws Exception {
        Article article = articleWithStatus(ArticleStatus.PENDING);
        when(articleService.submitMyArticle(1L, "alice")).thenReturn(article);

        mockMvc.perform(put("/api/my/articles/1/submit").principal(() -> "alice"))
                .andExpect(status().isOk());
    }

    @Test
    void putWithdrawRouteIsMapped() throws Exception {
        Article article = articleWithStatus(ArticleStatus.DRAFT);
        when(articleService.withdrawMyArticle(1L, "alice")).thenReturn(article);

        mockMvc.perform(put("/api/my/articles/1/withdraw").principal(() -> "alice"))
                .andExpect(status().isOk());
    }

    @Test
    void putApproveRouteIsMapped() throws Exception {
        Article article = articleWithStatus(ArticleStatus.PUBLISHED);
        when(articleService.approveArticle(1L)).thenReturn(article);

        mockMvc.perform(put("/api/admin/articles/1/approve"))
                .andExpect(status().isOk());
    }

    @Test
    void putRejectRouteIsMapped() throws Exception {
        Article article = articleWithStatus(ArticleStatus.REJECTED);
        article.setReviewReason("内容不完整");
        when(articleService.rejectArticle(1L, "内容不完整")).thenReturn(article);

        mockMvc.perform(put("/api/admin/articles/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"内容不完整\"}"))
                .andExpect(status().isOk());
    }

    private Article articleWithStatus(String status) {
        Article article = new Article();
        article.setStatus(status);
        return article;
    }
}
