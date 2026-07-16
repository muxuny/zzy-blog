package com.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.FavoriteArticleItem;
import com.blog.dto.FavoritePageQuery;
import com.blog.dto.FavoriteStatus;
import com.blog.service.FavoriteService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FavoriteControllerTest {

    private final FavoriteService favoriteService = mock(FavoriteService.class);

    private final FavoriteController controller = new FavoriteController(favoriteService);

    private final Principal principal = () -> "alice";

    @Test
    void endpointsUseCurrentPrincipal() {
        FavoritePageQuery query = new FavoritePageQuery();
        Page<FavoriteArticleItem> page = new Page<>(1, 10);
        when(favoriteService.getMyFavorites(query, "alice")).thenReturn(page);
        when(favoriteService.getFavoriteStatus(20L, "alice"))
                .thenReturn(new FavoriteStatus(true));

        assertEquals(200, controller.list(query, principal).getCode());
        assertTrue(controller.status(20L, principal).getData().isFavorited());
        assertEquals(200, controller.favorite(20L, principal).getCode());
        assertEquals(200, controller.unfavorite(20L, principal).getCode());

        verify(favoriteService).getMyFavorites(query, "alice");
        verify(favoriteService).getFavoriteStatus(20L, "alice");
        verify(favoriteService).favoriteArticle(20L, "alice");
        verify(favoriteService).unfavoriteArticle(20L, "alice");
    }

    @Test
    void allowsUsersAndAdmins() {
        PreAuthorize preAuthorize = FavoriteController.class.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals("hasAnyRole('USER', 'ADMIN')", preAuthorize.value());
    }
}
