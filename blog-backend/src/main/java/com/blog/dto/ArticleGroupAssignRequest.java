package com.blog.dto;

import lombok.Data;

import java.util.List;

@Data
public class ArticleGroupAssignRequest {
    private List<Long> groupIds;
}
