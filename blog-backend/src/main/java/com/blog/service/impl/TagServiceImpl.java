package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.BusinessException;
import com.blog.entity.Tag;
import com.blog.mapper.TagMapper;
import com.blog.service.TagService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public List<Tag> getAllTags() {
        return list(new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getName));
    }

    @Override
    public Tag createTag(String name) {
        Tag exist = getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, name));
        if (exist != null) throw new BusinessException("标签已存在");
        Tag tag = new Tag();
        tag.setName(name);
        save(tag);
        return tag;
    }
}
