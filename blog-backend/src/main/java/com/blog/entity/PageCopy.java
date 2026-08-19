package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 页面级导语配置，供公开页和后台页复用。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("page_copy")
public class PageCopy extends BaseEntity {
    private String copyKey;

    private String pageName;

    private String pageGroup;

    private String eyebrow;

    private String title;

    private String description;

    private Integer sortOrder;
}
