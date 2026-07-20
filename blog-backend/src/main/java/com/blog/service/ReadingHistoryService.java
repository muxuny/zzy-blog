package com.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blog.dto.ReadingHistoryItem;
import com.blog.dto.ReadingHistoryPageQuery;
import com.blog.entity.Article;

public interface ReadingHistoryService {

    void record(Article article, String username);

    IPage<ReadingHistoryItem> getHistory(ReadingHistoryPageQuery query, String username);

    ReadingHistoryItem getLastAvailable(String username);

    void deleteHistory(Long articleId, String username);

    void clearHistory(String username);
}
