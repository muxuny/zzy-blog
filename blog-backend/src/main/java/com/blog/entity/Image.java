package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("image")
public class Image extends BaseEntity {
    private String originalName;
    private String filename;
    private Long size;
    private String mimeType;
    private String url;
}
