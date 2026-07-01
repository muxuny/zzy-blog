package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章与作者自定义分组的关联关系。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article_group_relation")
public class ArticleGroupRelation extends BaseEntity {
    private Long articleId;

    private Long groupId;
}
