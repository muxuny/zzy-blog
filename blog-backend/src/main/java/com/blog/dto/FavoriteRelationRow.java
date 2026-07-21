package com.blog.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteRelationRow {
    private Long favoriteId;

    private Long articleId;

    private String titleSnapshot;

    private LocalDateTime favoritedAt;

    private boolean available;
}
