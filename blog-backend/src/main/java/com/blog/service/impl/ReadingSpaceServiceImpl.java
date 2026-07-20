package com.blog.service.impl;

import com.blog.common.BusinessException;
import com.blog.dto.FavoriteArticleItem;
import com.blog.dto.FavoritePageQuery;
import com.blog.dto.ReadingHistoryOverview;
import com.blog.dto.ReadingOverview;
import com.blog.entity.User;
import com.blog.service.FavoriteService;
import com.blog.service.ReadingHistoryService;
import com.blog.service.ReadingSpaceService;
import com.blog.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class ReadingSpaceServiceImpl implements ReadingSpaceService {

    private final ReadingHistoryService historyService;
    private final FavoriteService favoriteService;
    private final UserService userService;

    public ReadingSpaceServiceImpl(ReadingHistoryService historyService, FavoriteService favoriteService,
                                   UserService userService) {
        this.historyService = historyService;
        this.favoriteService = favoriteService;
        this.userService = userService;
    }

    @Override
    public ReadingOverview getOverview(String username) {
        User user = userService.getCurrentUser(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        ReadingHistoryOverview history = historyService.getOverview(5, user.getId());

        FavoritePageQuery favoriteQuery = new FavoritePageQuery();
        favoriteQuery.setPage(1);
        favoriteQuery.setSize(4);
        com.baomidou.mybatisplus.core.metadata.IPage<FavoriteArticleItem> favorites =
                favoriteService.getMyFavoritesForUser(favoriteQuery, user.getId());

        return new ReadingOverview(history.getLastRead(), history.getRecentHistory().getRecords(),
                history.getRecentHistory().getTotal(),
                favorites.getRecords(), favorites.getTotal());
    }
}
