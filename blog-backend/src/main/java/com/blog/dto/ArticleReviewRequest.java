package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ArticleReviewRequest {
    @NotBlank(message = "驳回原因不能为空")
    @Size(max = 500, message = "驳回原因不能超过 500 个字符")
    private String reason;
}
