package com.blog.dto;

import lombok.Data;

@Data
public class ArticlePageQuery {
    private long page = 1;
    private long size = 10;
    private Long tagId;
    private Long groupId;
    private String status;
    private String keyword;
    private String author;
    private Boolean ungrouped;
}
