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
    private boolean available;
}
