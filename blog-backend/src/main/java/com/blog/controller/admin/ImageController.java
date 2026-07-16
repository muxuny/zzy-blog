package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.entity.Image;
import com.blog.service.ImageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

/**
 * 图片素材接口：创作者可上传，管理员可查看和删除。
 */
@RestController
@RequestMapping("/api/admin/upload")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/image")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Result<Image> upload(@RequestParam("file") MultipartFile file,
                                 Principal principal) {
        return Result.success(imageService.uploadImage(file, principal.getName()));
    }

    @DeleteMapping("/image/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        imageService.deleteImage(id);
        return Result.success();
    }

    @GetMapping("/images")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResult<Image> list(@RequestParam(defaultValue = "1") long page,
                                   @RequestParam(defaultValue = "20") long size) {
        IPage<Image> result = imageService.page(new Page<>(page, size),
                new LambdaQueryWrapper<Image>().orderByDesc(Image::getCreatedAt));
        return PageResult.success(result);
    }
}
