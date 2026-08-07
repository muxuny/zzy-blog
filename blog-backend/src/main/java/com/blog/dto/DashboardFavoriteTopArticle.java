package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardFavoriteTopArticle {
    private Long articleId;
    private String title;
    private long favoriteCount;
}
