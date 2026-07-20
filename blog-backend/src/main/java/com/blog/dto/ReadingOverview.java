package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadingOverview {

    private ReadingHistoryItem lastRead;

    private List<ReadingHistoryItem> recentHistory;

    private long historyTotal;

    private List<FavoriteArticleItem> recentFavorites;

    private long favoriteTotal;
}
