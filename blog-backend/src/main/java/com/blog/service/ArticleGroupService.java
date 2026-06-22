package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.ArticleGroupRequest;
import com.blog.dto.ArticleGroupSummary;
import com.blog.entity.ArticleGroup;

import java.util.List;

public interface ArticleGroupService extends IService<ArticleGroup> {
    List<ArticleGroupSummary> listMyGroups(String username);

    ArticleGroup createMyGroup(ArticleGroupRequest request, String username);

    ArticleGroup updateMyGroup(Long id, ArticleGroupRequest request, String username);

    void deleteMyGroup(Long id, String username);
}
