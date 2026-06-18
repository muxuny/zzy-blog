package com.blog.dto;

import lombok.Data;

@Data
public class ArticlePageQuery {
    private long page = 1;
    private long size = 10;
    private Long tagId;
    private String status;
    private String keyword;
    private String author;
}
