package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.DashboardDateBucket;
import com.blog.dto.DashboardProgressBucket;
import com.blog.dto.ReadingHistoryRelationRow;
import com.blog.entity.ArticleReadingHistory;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

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

    ReadingHistoryRelationRow selectActiveHistory(@Param("userId") Long userId,
                                                  @Param("articleId") Long articleId);

    int updatePosition(@Param("userId") Long userId,
                       @Param("articleId") Long articleId,
                       @Param("progressPercent") Integer progressPercent,
                       @Param("scrollY") Integer scrollY,
                       @Param("anchorId") String anchorId,
                       @Param("anchorOffset") Integer anchorOffset,
                       @Param("articleUpdatedAt") LocalDateTime articleUpdatedAt,
                       @Param("username") String username,
                       @Param("now") LocalDateTime now);

    int deleteHistory(@Param("userId") Long userId,
                      @Param("articleId") Long articleId,
                      @Param("username") String username,
                      @Param("now") LocalDateTime now);

    int clearHistory(@Param("userId") Long userId,
                     @Param("username") String username,
                     @Param("now") LocalDateTime now);

    long selectRecentReadingCount(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);

    long selectActiveReaderCount(@Param("start") LocalDateTime start);

    long selectAverageProgress(@Param("start") LocalDateTime start);

    List<DashboardDateBucket> selectReadingDateBuckets(@Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end);

    List<DashboardProgressBucket> selectReadingProgressBuckets(@Param("start") LocalDateTime start);
}
