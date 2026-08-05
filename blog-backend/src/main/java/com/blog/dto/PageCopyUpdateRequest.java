package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 管理员更新页面导语的请求体。
 */
@Data
public class PageCopyUpdateRequest {
    @Size(max = 80, message = "眉标不能超过80个字符")
    private String eyebrow;

    @NotBlank(message = "页面标题不能为空")
    @Size(max = 160, message = "页面标题不能超过160个字符")
    private String title;

    @Size(max = 500, message = "页面描述不能超过500个字符")
    private String description;
}
