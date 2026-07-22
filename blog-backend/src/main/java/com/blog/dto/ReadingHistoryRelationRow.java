package com.blog.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReadingHistoryRelationRow {
    private Long historyId;
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
    private LocalDateTime articleUpdatedAt;
    private boolean available;
}
