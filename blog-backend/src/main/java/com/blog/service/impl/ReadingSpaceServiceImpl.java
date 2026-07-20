package com.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blog.dto.FavoriteArticleItem;
import com.blog.dto.FavoritePageQuery;
import com.blog.dto.ReadingHistoryItem;
import com.blog.dto.ReadingHistoryPageQuery;
import com.blog.dto.ReadingOverview;
import com.blog.service.FavoriteService;
import com.blog.service.ReadingHistoryService;
import com.blog.service.ReadingSpaceService;
import org.springframework.stereotype.Service;

@Service
public class ReadingSpaceServiceImpl implements ReadingSpaceService {

    private final ReadingHistoryService historyService;
    private final FavoriteService favoriteService;

    public ReadingSpaceServiceImpl(ReadingHistoryService historyService, FavoriteService favoriteService) {
        this.historyService = historyService;
        this.favoriteService = favoriteService;
    }

    @Override
    public ReadingOverview getOverview(String username) {
        ReadingHistoryPageQuery historyQuery = new ReadingHistoryPageQuery();
        historyQuery.setPage(1);
        historyQuery.setSize(5);
        IPage<ReadingHistoryItem> history = historyService.getHistory(historyQuery, username);

        FavoritePageQuery favoriteQuery = new FavoritePageQuery();
        favoriteQuery.setPage(1);
        favoriteQuery.setSize(4);
        IPage<FavoriteArticleItem> favorites = favoriteService.getMyFavorites(favoriteQuery, username);

        ReadingHistoryItem lastRead = historyService.getLastAvailable(username);
        return new ReadingOverview(lastRead, history.getRecords(), history.getTotal(),
                favorites.getRecords(), favorites.getTotal());
    }
}
