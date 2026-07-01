package com.blog.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 分页接口统一响应结构，保留前端分页组件需要的总数和页码信息。
 */
@Data
@AllArgsConstructor
public class PageResult<T> {
    private int code;

    private String message;

    private List<T> data;

    private long total;

    private long page;

    private long size;

    public static <T> PageResult<T> success(IPage<T> page) {
        return new PageResult<>(200, "success", page.getRecords(),
                page.getTotal(), page.getCurrent(), page.getSize());
    }
}
