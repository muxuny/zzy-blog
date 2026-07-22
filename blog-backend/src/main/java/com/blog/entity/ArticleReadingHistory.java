package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 登录用户的文章阅读历史关系。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article_reading_history")
public class ArticleReadingHistory extends BaseEntity {
    private Long userId;
    private Long articleId;
    private String titleSnapshot;
    private LocalDateTime firstReadAt;
    private LocalDateTime lastReadAt;
    private Integer readCount;
    private Integer progressPercent;
    private Integer scrollY;
    private String anchorId;
    private Integer anchorOffset;
    private LocalDateTime articleUpdatedAtSnapshot;
    private LocalDateTime positionUpdatedAt;
}
