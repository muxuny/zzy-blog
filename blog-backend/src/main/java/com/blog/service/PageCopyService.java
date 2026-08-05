package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.PageCopyItem;
import com.blog.dto.PageCopyUpdateRequest;
import com.blog.entity.PageCopy;

import java.util.List;

/**
 * 页面文案配置服务。
 */
public interface PageCopyService extends IService<PageCopy> {
    List<PageCopyItem> listAll();

    PageCopyItem updateCopy(String copyKey, PageCopyUpdateRequest request);

    List<PageCopyItem> resetAll();
}
