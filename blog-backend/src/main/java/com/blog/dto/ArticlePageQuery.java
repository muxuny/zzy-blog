package com.blog.dto;

import lombok.Data;

/**
 * 文章分页查询条件，公开阅读、创作中心和后台管理共用。
 */
@Data
public class ArticlePageQuery {
    private long page = 1;

    private long size = 10;

    /** 公开阅读侧按标签筛选。 */
    private Long tagId;

    /** 创作中心按作者自定义分组筛选。 */
    private Long groupId;

    /** 后台或创作中心按审核状态筛选。 */
    private String status;

    /** 后台或创作中心按公开/私密可见性筛选。 */
    private String visibility;

    /** 标题、摘要等文本关键词。 */
    private String keyword;

    /** 后台管理侧按作者筛选。 */
    private String author;

    /** 创作中心筛选未分组文章。 */
    private Boolean ungrouped;
}
