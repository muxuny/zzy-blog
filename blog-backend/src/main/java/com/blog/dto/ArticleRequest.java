package com.blog.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class ArticleRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;
    private String summary;
    private String coverImage;
    private String status;
    private String visibility;

    private List<Long> tagIds;

    private List<Long> groupIds;
}
