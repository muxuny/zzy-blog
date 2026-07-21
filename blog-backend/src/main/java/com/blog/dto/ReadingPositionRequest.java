package com.blog.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReadingPositionRequest {
    private Integer progressPercent;
    private Integer scrollY;
    private String anchorId;
    private Integer anchorOffset;
    private LocalDateTime articleUpdatedAt;
}
