package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Tag;
import java.util.List;

/**
 * 标签查询与后台维护服务。
 */
public interface TagService extends IService<Tag> {
    List<Tag> getAllTags();

    Tag createTag(String name);
}
