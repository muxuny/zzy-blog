package com.blog.dto;

import lombok.Data;

@Data
public class FavoritePageQuery {
    private long page = 1;

    private long size = 10;

    private String keyword;

    private Long tagId;
}
