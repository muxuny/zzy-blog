package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Article;
import org.apache.ibatis.annotations.Param;

/**
 * 文章基础数据访问接口。
 */
public interface ArticleMapper extends BaseMapper<Article> {

    long selectTotalViewCount();

    long selectLowViewArticleCount(@Param("threshold") long threshold);
}
