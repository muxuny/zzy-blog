package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.ArticlePageQuery;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public PageResult<Article> list(ArticlePageQuery query) {
        return PageResult.success(articleService.getPublicPage(query));
    }

    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        return Result.success(articleService.getPublicDetail(id));
    }
}
