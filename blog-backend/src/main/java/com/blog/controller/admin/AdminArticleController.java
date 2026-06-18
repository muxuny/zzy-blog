package com.blog.controller.admin;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.ArticlePageQuery;
import com.blog.dto.ArticleRequest;
import com.blog.dto.ArticleReviewRequest;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/articles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminArticleController {

    private final ArticleService articleService;

    public AdminArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public PageResult<Article> list(ArticlePageQuery query) {
        return PageResult.success(articleService.getAdminPage(query));
    }

    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        return Result.success(articleService.getAdminDetail(id));
    }

    @PostMapping
    public Result<Article> create(@Valid @RequestBody ArticleRequest request) {
        return Result.success(articleService.createAdminArticle(request));
    }

    @PutMapping("/{id}")
    public Result<Article> update(@PathVariable Long id,
                                  @Valid @RequestBody ArticleRequest request) {
        return Result.success(articleService.updateAdminArticle(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.deleteAdminArticle(id);
        return Result.success();
    }

    @PutMapping("/{id}/approve")
    public Result<Article> approve(@PathVariable Long id) {
        return Result.success(articleService.approveArticle(id));
    }

    @PutMapping("/{id}/reject")
    public Result<Article> reject(@PathVariable Long id,
                                  @Valid @RequestBody ArticleReviewRequest request) {
        return Result.success(articleService.rejectArticle(id, request.getReason()));
    }
}
