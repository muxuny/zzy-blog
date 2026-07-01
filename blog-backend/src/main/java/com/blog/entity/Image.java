package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 上传图片元数据，文件实体保存在本地上传目录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("image")
public class Image extends BaseEntity {
    private String originalName;

    private String filename;

    private Long size;

    private String mimeType;

    /** 前端访问图片时使用的公开 URL。 */
    private String url;
}
