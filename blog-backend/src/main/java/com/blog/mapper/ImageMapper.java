package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Image;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 图片素材基础数据访问接口。
 */
public interface ImageMapper extends BaseMapper<Image> {

    long selectTotalImageSize();

    long selectRecentImageCount(@Param("start") LocalDateTime start);
}
