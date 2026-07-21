package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.dto.ReadingPositionState;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 文章主体，包含审核状态、可见性以及前端展示所需的聚合字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article")
public class Article extends BaseEntity {
    private String title;

    private String content;

    private String summary;

    private String coverImage;

    /** 审核状态，取值见 {@link com.blog.common.ArticleStatus}。 */
    private String status;

    /** 可见性，取值见 {@link com.blog.common.ArticleVisibility}。 */
    private String visibility;

    /** 审核驳回原因，仅驳回状态下对作者有意义。 */
    private String reviewReason;

    /** 阅读次数，公开详情接口访问时递增。 */
    private Integer viewCount;

    /** 前端展示字段，不直接映射数据库列。 */
    @TableField(exist = false)
    private List<Tag> tags;

    /** 前端展示字段，不直接映射数据库列。 */
    @TableField(exist = false)
    private List<ArticleGroup> groups;

    /** 作者昵称或用户名，不直接映射数据库列。 */
    @TableField(exist = false)
    private String authorName;

    /** 当前登录读者的阅读位置状态，不直接映射数据库列。 */
    @TableField(exist = false)
    private ReadingPositionState readingPosition;
}
