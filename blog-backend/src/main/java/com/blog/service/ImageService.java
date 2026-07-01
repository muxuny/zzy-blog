package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Image;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传与素材管理服务。
 */
public interface ImageService extends IService<Image> {
    Image uploadImage(MultipartFile file, String username);

    void deleteImage(Long id);
}
