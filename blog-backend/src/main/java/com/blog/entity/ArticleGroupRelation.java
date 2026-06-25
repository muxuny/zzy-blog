package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article_group_relation")
public class ArticleGroupRelation extends BaseEntity {
    private Long articleId;
    private Long groupId;
}
