package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 管理员驳回文章时填写的审核意见。
 */
@Data
public class ArticleReviewRequest {
    @NotBlank(message = "驳回原因不能为空")
    @Size(max = 500, message = "驳回原因不能超过 500 个字符")
    private String reason;
}
