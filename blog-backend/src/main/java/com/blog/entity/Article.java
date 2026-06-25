package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article")
public class Article extends BaseEntity {
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private String status;
    private String reviewReason;
    private Integer viewCount;

    @TableField(exist = false)
    private List<Tag> tags;

    @TableField(exist = false)
    private List<ArticleGroup> groups;

    @TableField(exist = false)
    private String authorName;
}
