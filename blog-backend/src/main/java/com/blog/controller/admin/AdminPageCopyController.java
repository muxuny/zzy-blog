package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.dto.PageCopyItem;
import com.blog.dto.PageCopyUpdateRequest;
import com.blog.service.PageCopyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 后台页面文案配置接口。
 */
@Validated
@RestController
@RequestMapping("/api/admin/page-copies")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPageCopyController {

    private final PageCopyService pageCopyService;

    public AdminPageCopyController(PageCopyService pageCopyService) {
        this.pageCopyService = pageCopyService;
    }

    @GetMapping
    public Result<List<PageCopyItem>> list() {
        return Result.success(pageCopyService.listAll());
    }

    @PutMapping("/{copyKey}")
    public Result<PageCopyItem> update(@PathVariable String copyKey,
                                       @Valid @RequestBody PageCopyUpdateRequest request) {
        return Result.success(pageCopyService.updateCopy(copyKey, request));
    }

    @PostMapping("/reset")
    public Result<List<PageCopyItem>> reset() {
        return Result.success(pageCopyService.resetAll());
    }
}
