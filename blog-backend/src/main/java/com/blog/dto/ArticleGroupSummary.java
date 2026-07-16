package com.blog.dto;

import lombok.Data;

/**
 * 创作中心分组列表摘要，包含分组下文章数量。
 */
@Data
public class ArticleGroupSummary {
    private Long id;

    private String name;

    private Long articleCount;
}
