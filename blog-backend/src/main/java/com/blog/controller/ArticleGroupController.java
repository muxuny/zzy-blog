package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.ArticleGroupRequest;
import com.blog.dto.ArticleGroupSummary;
import com.blog.entity.ArticleGroup;
import com.blog.service.ArticleGroupService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

/**
 * 创作者文章分组接口。
 */
@RestController
@RequestMapping("/api/my/article-groups")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class ArticleGroupController {

    private final ArticleGroupService articleGroupService;

    public ArticleGroupController(ArticleGroupService articleGroupService) {
        this.articleGroupService = articleGroupService;
    }

    @GetMapping
    public Result<List<ArticleGroupSummary>> list(Principal principal) {
        return Result.success(articleGroupService.listMyGroups(principal.getName()));
    }

    @PostMapping
    public Result<ArticleGroup> create(@Valid @RequestBody ArticleGroupRequest request, Principal principal) {
        return Result.success(articleGroupService.createMyGroup(request, principal.getName()));
    }

    @PutMapping("/{id}")
    public Result<ArticleGroup> update(@PathVariable Long id,
                                       @Valid @RequestBody ArticleGroupRequest request,
                                       Principal principal) {
        return Result.success(articleGroupService.updateMyGroup(id, request, principal.getName()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Principal principal) {
        articleGroupService.deleteMyGroup(id, principal.getName());
        return Result.success();
    }
}
