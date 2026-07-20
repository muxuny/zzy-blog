package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.ReadingHistoryRelationRow;
import com.blog.entity.ArticleReadingHistory;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface ArticleReadingHistoryMapper extends BaseMapper<ArticleReadingHistory> {

    int upsertHistory(@Param("id") Long id,
                      @Param("userId") Long userId,
                      @Param("articleId") Long articleId,
                      @Param("titleSnapshot") String titleSnapshot,
                      @Param("username") String username,
                      @Param("now") LocalDateTime now);

    IPage<ReadingHistoryRelationRow> selectHistoryPage(Page<ReadingHistoryRelationRow> page,
                                                        @Param("userId") Long userId);

    ReadingHistoryRelationRow selectLastAvailable(@Param("userId") Long userId);

    int deleteHistory(@Param("userId") Long userId,
                      @Param("articleId") Long articleId,
                      @Param("username") String username,
                      @Param("now") LocalDateTime now);

    int clearHistory(@Param("userId") Long userId,
                     @Param("username") String username,
                     @Param("now") LocalDateTime now);
}
