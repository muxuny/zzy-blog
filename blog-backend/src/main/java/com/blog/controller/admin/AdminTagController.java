package com.blog.controller.admin;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.entity.Tag;
import com.blog.service.TagService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台标签管理接口。
 */
@RestController
@RequestMapping("/api/admin/tags")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTagController {

    private final TagService tagService;

    public AdminTagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public PageResult<Tag> list(@RequestParam(defaultValue = "1") long page,
                                @RequestParam(defaultValue = "20") long size) {
        return PageResult.success(tagService.getAdminPage(page, size));
    }

    @PostMapping
    public Result<Tag> create(@RequestBody Map<String, String> body) {
        return Result.success(tagService.createTag(body.get("name")));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.removeById(id);
        return Result.success();
    }
}
