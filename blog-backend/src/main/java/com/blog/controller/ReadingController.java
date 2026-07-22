package com.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.ReadingHistoryItem;
import com.blog.dto.ReadingHistoryPageQuery;
import com.blog.dto.ReadingOverview;
import com.blog.dto.ReadingPositionRequest;
import com.blog.service.ReadingHistoryService;
import com.blog.service.ReadingSpaceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/my/reading")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class ReadingController {

    private final ReadingSpaceService readingSpaceService;
    private final ReadingHistoryService readingHistoryService;

    public ReadingController(ReadingSpaceService readingSpaceService,
                             ReadingHistoryService readingHistoryService) {
        this.readingSpaceService = readingSpaceService;
        this.readingHistoryService = readingHistoryService;
    }

    @GetMapping("/overview")
    public Result<ReadingOverview> overview(Principal principal) {
        return Result.success(readingSpaceService.getOverview(principal.getName()));
    }

    @GetMapping("/history")
    public PageResult<ReadingHistoryItem> history(ReadingHistoryPageQuery query, Principal principal) {
        IPage<ReadingHistoryItem> history = readingHistoryService.getHistory(query, principal.getName());
        return PageResult.success(history);
    }

    @DeleteMapping("/history/{articleId}")
    public Result<Void> deleteHistory(@PathVariable Long articleId, Principal principal) {
        readingHistoryService.deleteHistory(articleId, principal.getName());
        return Result.success();
    }

    @PutMapping("/history/{articleId}/position")
    public Result<Void> savePosition(@PathVariable Long articleId,
                                     @RequestBody ReadingPositionRequest request,
                                     Principal principal) {
        readingHistoryService.savePosition(articleId, request, principal.getName());
        return Result.success();
    }

    @DeleteMapping("/history")
    public Result<Void> clearHistory(Principal principal) {
        readingHistoryService.clearHistory(principal.getName());
        return Result.success();
    }
}
