package com.blog.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量调整文章所属分组的请求。
 */
@Data
public class ArticleGroupAssignRequest {
    private List<Long> groupIds;
}
