package com.blog.dto;

import lombok.Data;

/**
 * 创作者调整文章可见性请求。
 */
@Data
public class ArticleVisibilityRequest {
    private String visibility;
}
