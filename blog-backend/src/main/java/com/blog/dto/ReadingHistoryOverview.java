package com.blog.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

@Data
public class ReadingHistoryOverview {

    private ReadingHistoryItem lastRead;

    private IPage<ReadingHistoryItem> recentHistory;
}
