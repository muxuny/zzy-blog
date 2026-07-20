package com.blog.dto;

import lombok.Data;

@Data
public class ReadingHistoryPageQuery {
    private long page = 1;
    private long size = 10;
}
