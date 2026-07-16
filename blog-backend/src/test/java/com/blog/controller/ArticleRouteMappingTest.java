package com.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.ArticleStatus;
import com.blog.config.JwtAuthFilter;
import com.blog.config.JwtUtil;
import com.blog.config.SecurityConfig;
import com.blog.controller.admin.AdminArticleController;
import com.blog.dto.FavoriteArticleItem;
import com.blog.dto.FavoriteStatus;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import com.blog.service.FavoriteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        MyArticleController.class,
        AdminArticleController.class,
        FavoriteController.class
})
@ContextConfiguration(classes = {
        MyArticleController.class,
        AdminArticleController.class,
        FavoriteController.class,
        SecurityConfig.class,
        JwtAuthFilter.class
})
class ArticleRouteMappingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private FavoriteService favoriteService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void putSubmitRouteIsMapped() throws Exception {
        Article article = articleWithStatus(ArticleStatus.PENDING);
        when(articleService.submitMyArticle(1L, "alice")).thenReturn(article);

        mockMvc.perform(put("/api/my/articles/1/submit")
                        .with(user("alice").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void putWithdrawRouteIsMapped() throws Exception {
        Article article = articleWithStatus(ArticleStatus.DRAFT);
        when(articleService.withdrawMyArticle(1L, "alice")).thenReturn(article);

        mockMvc.perform(put("/api/my/articles/1/withdraw")
                        .with(user("alice").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void putMyArticleGroupsRouteIsMapped() throws Exception {
        Article article = articleWithStatus(ArticleStatus.PUBLISHED);
        when(articleService.updateMyArticleGroups(1L, Arrays.asList(10L), "alice")).thenReturn(article);

        mockMvc.perform(put("/api/my/articles/1/groups")
                        .with(user("alice").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"groupIds\":[10]}"))
                .andExpect(status().isOk());
    }

    @Test
    void putMyArticleVisibilityRouteIsMapped() throws Exception {
        Article article = articleWithStatus(ArticleStatus.PUBLISHED);
        article.setVisibility("private");
        when(articleService.updateMyArticleVisibility(1L, "private", "alice")).thenReturn(article);

        mockMvc.perform(put("/api/my/articles/1/visibility")
                        .with(user("alice").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"visibility\":\"private\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putApproveRouteIsMapped() throws Exception {
        Article article = articleWithStatus(ArticleStatus.PUBLISHED);
        when(articleService.approveArticle(1L)).thenReturn(article);

        mockMvc.perform(put("/api/admin/articles/1/approve")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void putRejectRouteIsMapped() throws Exception {
        Article article = articleWithStatus(ArticleStatus.REJECTED);
        article.setReviewReason("内容不完整");
        when(articleService.rejectArticle(1L, "内容不完整")).thenReturn(article);

        mockMvc.perform(put("/api/admin/articles/1/reject")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"内容不完整\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousFavoriteRequestsAreForbidden() throws Exception {
        mockMvc.perform(get("/api/my/favorites"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/my/favorites/20/status"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/my/favorites/20"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/my/favorites/20"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getFavoritesRouteUsesAuthenticatedUserAndQuery() throws Exception {
        Page<FavoriteArticleItem> page = new Page<>(2, 5);
        when(favoriteService.getMyFavorites(any(), eq("alice"))).thenReturn(page);

        mockMvc.perform(get("/api/my/favorites")
                        .with(user("alice").roles("USER"))
                        .param("page", "2")
                        .param("size", "5")
                        .param("keyword", "spring")
                        .param("tagId", "3"))
                .andExpect(status().isOk());

        verify(favoriteService).getMyFavorites(argThat(query ->
                query.getPage() == 2
                        && query.getSize() == 5
                        && "spring".equals(query.getKeyword())
                        && Long.valueOf(3L).equals(query.getTagId())), eq("alice"));
    }

    @Test
    void getFavoriteStatusRouteUsesAuthenticatedUserAndArticleId() throws Exception {
        when(favoriteService.getFavoriteStatus(20L, "alice"))
                .thenReturn(new FavoriteStatus(true));

        mockMvc.perform(get("/api/my/favorites/20/status")
                        .with(user("alice").roles("USER")))
                .andExpect(status().isOk());

        verify(favoriteService).getFavoriteStatus(20L, "alice");
    }

    @Test
    void putFavoriteRouteUsesAuthenticatedUserAndArticleId() throws Exception {
        mockMvc.perform(put("/api/my/favorites/20")
                        .with(user("alice").roles("USER")))
                .andExpect(status().isOk());

        verify(favoriteService).favoriteArticle(20L, "alice");
    }

    @Test
    void deleteFavoriteRouteUsesAuthenticatedUserAndArticleId() throws Exception {
        mockMvc.perform(delete("/api/my/favorites/20")
                        .with(user("alice").roles("USER")))
                .andExpect(status().isOk());

        verify(favoriteService).unfavoriteArticle(20L, "alice");
    }

    private Article articleWithStatus(String status) {
        Article article = new Article();
        article.setStatus(status);
        return article;
    }
}
