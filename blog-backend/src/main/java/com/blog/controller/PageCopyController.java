package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.PageCopyItem;
import com.blog.service.PageCopyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开页面文案配置接口。
 */
@RestController
@RequestMapping("/api/page-copies")
public class PageCopyController {

    private final PageCopyService pageCopyService;

    public PageCopyController(PageCopyService pageCopyService) {
        this.pageCopyService = pageCopyService;
    }

    @GetMapping
    public Result<List<PageCopyItem>> list() {
        return Result.success(pageCopyService.listAll());
    }
}
