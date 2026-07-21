package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.ArticleNeighbors;
import com.blog.dto.ArticlePageQuery;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import com.blog.service.ReadingHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * 公开阅读侧文章接口。
 */
@RestController
@RequestMapping("/api/articles")
@Slf4j
public class ArticleController {

    private final ArticleService articleService;
    private final ReadingHistoryService readingHistoryService;

    public ArticleController(ArticleService articleService, ReadingHistoryService readingHistoryService) {
        this.articleService = articleService;
        this.readingHistoryService = readingHistoryService;
    }

    @GetMapping
    public PageResult<Article> list(ArticlePageQuery query) {
        return PageResult.success(articleService.getPublicPage(query));
    }

    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id, Principal principal) {
        Article article = articleService.getPublicDetail(id);
        if (principal != null) {
            try {
                readingHistoryService.record(article, principal.getName());
            } catch (RuntimeException exception) {
                log.error("Failed to record reading history for article {} and user {}",
                        article.getId(), principal.getName(), exception);
            }
            try {
                article.setReadingPosition(readingHistoryService.getPositionState(article, principal.getName()));
            } catch (RuntimeException exception) {
                log.error("Failed to attach reading position for article {} and user {}",
                        article.getId(), principal.getName(), exception);
            }
        }
        return Result.success(article);
    }

    @GetMapping("/{id}/neighbors")
    public Result<ArticleNeighbors> neighbors(@PathVariable Long id) {
        return Result.success(articleService.getPublicNeighbors(id));
    }

    @GetMapping("/{id}/related")
    public Result<List<Article>> related(@PathVariable Long id,
                                         @RequestParam(defaultValue = "4") Integer size) {
        return Result.success(articleService.getRelatedArticles(id, size));
    }
}
