package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.BusinessException;
import com.blog.entity.Image;
import com.blog.mapper.ImageMapper;
import com.blog.service.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 图片素材服务实现，负责本地文件写入、元数据保存和删除。
 */
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements ImageService {

    @Value("${blog.upload-dir}")
    private String uploadDir;

    @Override
    public Image uploadImage(MultipartFile file, String username) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + ext;

        try {
            File dest = new File(uploadDir, filename);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        }

        Image image = new Image();
        image.setOriginalName(originalName);
        image.setFilename(filename);
        image.setSize(file.getSize());
        image.setMimeType(file.getContentType());
        image.setUrl("/uploads/" + filename);
        image.setCreatedBy(username);
        save(image);
        return image;
    }

    @Override
    public void deleteImage(Long id) {
        Image image = getById(id);
        if (image == null) {
            throw new BusinessException("图片不存在");
        }

        File file = new File(uploadDir, image.getFilename());
        if (file.exists()) {
            file.delete();
        }
        removeById(id);
    }
}
