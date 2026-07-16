package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 创建或重命名文章分组的请求。
 */
@Data
public class ArticleGroupRequest {
    @NotBlank(message = "分组名称不能为空")
    @Size(max = 50, message = "分组名称不能超过50个字符")
    private String name;
}
