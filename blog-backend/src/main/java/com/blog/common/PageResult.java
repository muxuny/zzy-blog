package com.blog.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

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
