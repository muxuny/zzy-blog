package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.FavoriteArticleItem;
import com.blog.dto.FavoritePageQuery;
import com.blog.dto.FavoriteStatus;
import com.blog.service.FavoriteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/my/favorites")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public PageResult<FavoriteArticleItem> list(FavoritePageQuery query, Principal principal) {
        return PageResult.success(favoriteService.getMyFavorites(query, principal.getName()));
    }

    @GetMapping("/{articleId}/status")
    public Result<FavoriteStatus> status(@PathVariable Long articleId, Principal principal) {
        return Result.success(favoriteService.getFavoriteStatus(articleId, principal.getName()));
    }

    @PutMapping("/{articleId}")
    public Result<Void> favorite(@PathVariable Long articleId, Principal principal) {
        favoriteService.favoriteArticle(articleId, principal.getName());
        return Result.success();
    }

    @DeleteMapping("/{articleId}")
    public Result<Void> unfavorite(@PathVariable Long articleId, Principal principal) {
        favoriteService.unfavoriteArticle(articleId, principal.getName());
        return Result.success();
    }
}
