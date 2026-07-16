package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 创建和编辑文章的请求参数。
 */
@Data
public class ArticleRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;

    private String summary;

    private String coverImage;

    /** 作者保存草稿或提交审核时传入的目标状态。 */
    private String status;

    /** 文章公开或仅自己可见。 */
    private String visibility;

    /** 文章关联的标签 ID 列表。 */
    private List<Long> tagIds;

    /** 作者自定义分组 ID 列表。 */
    private List<Long> groupIds;
}
