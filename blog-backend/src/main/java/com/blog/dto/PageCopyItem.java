package com.blog.dto;

import com.blog.entity.PageCopy;
import lombok.Data;

/**
 * 页面文案配置返回项。
 */
@Data
public class PageCopyItem {
    private String copyKey;

    private String pageName;

    private String pageGroup;

    private String eyebrow;

    private String title;

    private String description;

    private Integer sortOrder;

    public static PageCopyItem from(PageCopy pageCopy) {
        PageCopyItem item = new PageCopyItem();
        item.setCopyKey(pageCopy.getCopyKey());
        item.setPageName(pageCopy.getPageName());
        item.setPageGroup(pageCopy.getPageGroup());
        item.setEyebrow(pageCopy.getEyebrow());
        item.setTitle(pageCopy.getTitle());
        item.setDescription(pageCopy.getDescription());
        item.setSortOrder(pageCopy.getSortOrder());
        return item;
    }
}
