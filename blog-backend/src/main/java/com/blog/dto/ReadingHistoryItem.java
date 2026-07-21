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
    private Integer progressPercent;
    private LocalDateTime positionUpdatedAt;
    private boolean canResume;
    private Integer resumeScrollY;
    private String resumeAnchorId;
    private Integer resumeAnchorOffset;
    private boolean available;
    private String unavailableMessage;
}
