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
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    private static final long ARTICLE_ID = 1986429356912345088L;
    private static final long TAG_ID = 1986429356912345089L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FavoriteController favoriteController;

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
        mockMvc.perform(get("/api/my/favorites/{articleId}/status", ARTICLE_ID))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/my/favorites/{articleId}", ARTICLE_ID))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/my/favorites/{articleId}", ARTICLE_ID))
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
                        .param("tagId", Long.toString(TAG_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(5));

        verify(favoriteService).getMyFavorites(argThat(query ->
                query.getPage() == 2
                        && query.getSize() == 5
                        && "spring".equals(query.getKeyword())
                        && Long.valueOf(TAG_ID).equals(query.getTagId())), eq("alice"));
    }

    @Test
    void getFavoriteStatusRouteUsesAuthenticatedUserAndArticleId() throws Exception {
        when(favoriteService.getFavoriteStatus(ARTICLE_ID, "alice"))
                .thenReturn(new FavoriteStatus(true));

        mockMvc.perform(get("/api/my/favorites/{articleId}/status", ARTICLE_ID)
                        .with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.favorited").value(true));

        verify(favoriteService).getFavoriteStatus(ARTICLE_ID, "alice");
    }

    @Test
    void putFavoriteRouteUsesAuthenticatedUserAndArticleId() throws Exception {
        mockMvc.perform(put("/api/my/favorites/{articleId}", ARTICLE_ID)
                        .with(user("alice").roles("USER")))
                .andExpect(status().isOk());

        verify(favoriteService).favoriteArticle(ARTICLE_ID, "alice");
    }

    @Test
    void deleteFavoriteRouteUsesAuthenticatedUserAndArticleId() throws Exception {
        mockMvc.perform(delete("/api/my/favorites/{articleId}", ARTICLE_ID)
                        .with(user("alice").roles("USER")))
                .andExpect(status().isOk());

        verify(favoriteService).unfavoriteArticle(ARTICLE_ID, "alice");
    }

    @Test
    void adminCanReachFavoriteController() throws Exception {
        when(favoriteService.getFavoriteStatus(ARTICLE_ID, "admin"))
                .thenReturn(new FavoriteStatus(true));

        mockMvc.perform(get("/api/my/favorites/{articleId}/status", ARTICLE_ID)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited").value(true));

        verify(favoriteService).getFavoriteStatus(ARTICLE_ID, "admin");
    }

    @Test
    void auditorCannotReachFavoriteController() throws Exception {
        mockMvc.perform(get("/api/my/favorites/{articleId}/status", ARTICLE_ID)
                        .with(user("auditor").roles("AUDITOR")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(favoriteService);
    }

    @Test
    @WithMockUser(username = "auditor", roles = "AUDITOR")
    void favoriteControllerProxyEnforcesMethodRole() {
        assertTrue(AopUtils.isAopProxy(favoriteController));

        assertThrows(AccessDeniedException.class,
                () -> favoriteController.status(ARTICLE_ID, () -> "auditor"));

        verifyNoInteractions(favoriteService);
    }

    private Article articleWithStatus(String status) {
        Article article = new Article();
        article.setStatus(status);
        return article;
    }
}
