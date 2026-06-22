package com.blog.dto;

import lombok.Data;

@Data
public class ArticleGroupSummary {
    private Long id;
    private String name;
    private Long articleCount;
}
