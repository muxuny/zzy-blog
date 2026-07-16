package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.ArticleFavorite;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface ArticleFavoriteMapper extends BaseMapper<ArticleFavorite> {

    int upsertFavorite(@Param("id") Long id,
                       @Param("userId") Long userId,
                       @Param("articleId") Long articleId,
                       @Param("titleSnapshot") String titleSnapshot,
                       @Param("username") String username,
                       @Param("now") LocalDateTime now);

    int cancelFavorite(@Param("userId") Long userId,
                       @Param("articleId") Long articleId,
                       @Param("username") String username,
                       @Param("now") LocalDateTime now);

    long countActiveFavorite(@Param("userId") Long userId,
                             @Param("articleId") Long articleId);
}
