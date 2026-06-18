package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.entity.Tag;
import com.blog.service.TagService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/tags")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTagController {

    private final TagService tagService;

    public AdminTagController(TagService tagService) {
        this.tagService = tagService;
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
