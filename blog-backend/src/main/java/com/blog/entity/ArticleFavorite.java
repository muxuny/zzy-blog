package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 登录用户与公开文章的收藏关系。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article_favorite")
public class ArticleFavorite extends BaseEntity {
    private Long userId;
    private Long articleId;
    private String titleSnapshot;
}
