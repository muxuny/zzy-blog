package com.blog.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReadingHistoryItem {
    private Long articleId;
    private String title;
    private String summary;
    private String coverImage;
    private String authorName;
    private Integer viewCount;
    private LocalDateTime firstReadAt;
    private LocalDateTime lastReadAt;
    private Integer readCount;
    private boolean available;
    private String unavailableMessage;
}
