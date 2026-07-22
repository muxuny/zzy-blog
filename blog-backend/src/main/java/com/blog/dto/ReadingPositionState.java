package com.blog.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReadingPositionState {
    private Integer progressPercent;
    private LocalDateTime positionUpdatedAt;
    private boolean canResume;
    private Integer resumeScrollY;
    private String resumeAnchorId;
    private Integer resumeAnchorOffset;
}
