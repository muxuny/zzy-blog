package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.ArticleGroupAssignRequest;
import com.blog.dto.ArticlePageQuery;
import com.blog.dto.ArticleRequest;
import com.blog.dto.ArticleVisibilityRequest;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/my/articles")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class MyArticleController {

    private final ArticleService articleService;

    public MyArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public PageResult<Article> list(ArticlePageQuery query, Principal principal) {
        return PageResult.success(articleService.getMyPage(query, principal.getName()));
    }

    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id, Principal principal) {
        return Result.success(articleService.getMyDetail(id, principal.getName()));
    }

    @PostMapping
    public Result<Article> create(@Valid @RequestBody ArticleRequest request, Principal principal) {
        return Result.success(articleService.createMyArticle(request, principal.getName()));
    }

    @PutMapping("/{id}")
    public Result<Article> update(@PathVariable Long id,
                                  @Valid @RequestBody ArticleRequest request,
                                  Principal principal) {
        return Result.success(articleService.updateMyArticle(id, request, principal.getName()));
    }

    @PutMapping("/{id}/groups")
    public Result<Article> updateGroups(@PathVariable Long id,
                                        @RequestBody ArticleGroupAssignRequest request,
                                        Principal principal) {
        return Result.success(articleService.updateMyArticleGroups(id, request.getGroupIds(), principal.getName()));
    }

    @PutMapping("/{id}/visibility")
    public Result<Article> updateVisibility(@PathVariable Long id,
                                            @RequestBody ArticleVisibilityRequest request,
                                            Principal principal) {
        return Result.success(articleService.updateMyArticleVisibility(id, request.getVisibility(), principal.getName()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Principal principal) {
        articleService.deleteMyArticle(id, principal.getName());
        return Result.success();
    }

    @PutMapping("/{id}/submit")
    public Result<Article> submit(@PathVariable Long id, Principal principal) {
        return Result.success(articleService.submitMyArticle(id, principal.getName()));
    }

    @PutMapping("/{id}/withdraw")
    public Result<Article> withdraw(@PathVariable Long id, Principal principal) {
        return Result.success(articleService.withdrawMyArticle(id, principal.getName()));
    }
}
