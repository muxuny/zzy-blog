package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 作者自定义文章分组，归属关系通过 BaseEntity.createdBy 区分。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article_group")
public class ArticleGroup extends BaseEntity {
    private String name;
}
