package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.BusinessException;
import com.blog.dto.ArticleGroupRequest;
import com.blog.dto.ArticleGroupSummary;
import com.blog.entity.ArticleGroup;
import com.blog.entity.ArticleGroupRelation;
import com.blog.mapper.ArticleGroupMapper;
import com.blog.mapper.ArticleGroupRelationMapper;
import com.blog.service.ArticleGroupService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleGroupServiceImpl extends ServiceImpl<ArticleGroupMapper, ArticleGroup>
        implements ArticleGroupService {

    private final ArticleGroupRelationMapper articleGroupRelationMapper;

    public ArticleGroupServiceImpl(ArticleGroupMapper articleGroupMapper,
                                   ArticleGroupRelationMapper articleGroupRelationMapper) {
        this.articleGroupRelationMapper = articleGroupRelationMapper;
    }

    @Override
    public List<ArticleGroupSummary> listMyGroups(String username) {
        return listGroupsByOwner(username).stream()
                .map(group -> {
                    ArticleGroupSummary summary = new ArticleGroupSummary();
                    summary.setId(group.getId());
                    summary.setName(group.getName());
                    summary.setArticleCount(countArticlesByGroupId(group.getId()));
                    return summary;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ArticleGroup createMyGroup(ArticleGroupRequest request, String username) {
        String name = normalizeName(request);
        if (findGroupByNameAndOwner(name, username) != null) {
            throw new BusinessException("分组已存在");
        }

        ArticleGroup group = new ArticleGroup();
        group.setName(name);
        group.setCreatedBy(username);
        try {
            save(group);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("分组已存在");
        }
        return group;
    }

    @Override
    @Transactional
    public ArticleGroup updateMyGroup(Long id, ArticleGroupRequest request, String username) {
        ArticleGroup group = requireOwnedGroup(id, username);
        String name = normalizeName(request);
        ArticleGroup sameName = findGroupByNameAndOwner(name, username);
        if (sameName != null && !sameName.getId().equals(id)) {
            throw new BusinessException("分组已存在");
        }

        group.setName(name);
        try {
            updateById(group);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("分组已存在");
        }
        return group;
    }

    @Override
    @Transactional
    public void deleteMyGroup(Long id, String username) {
        requireOwnedGroup(id, username);
        removeById(id);
        deleteRelationsByGroupId(id);
    }

    private ArticleGroup requireOwnedGroup(Long id, String username) {
        ArticleGroup group = findGroupByIdAndOwner(id, username);
        if (group == null) {
            throw new BusinessException("分组不存在");
        }
        return group;
    }

    private String normalizeName(ArticleGroupRequest request) {
        String name = request == null ? null : request.getName();
        name = name == null ? "" : name.trim();
        if (name.isEmpty()) {
            throw new BusinessException("分组名称不能为空");
        }
        return name;
    }

    protected List<ArticleGroup> listGroupsByOwner(String username) {
        return list(new LambdaQueryWrapper<ArticleGroup>()
                .eq(ArticleGroup::getCreatedBy, username)
                .orderByAsc(ArticleGroup::getCreatedAt)
                .orderByAsc(ArticleGroup::getId));
    }

    protected ArticleGroup findGroupByIdAndOwner(Long id, String username) {
        return getOne(new LambdaQueryWrapper<ArticleGroup>()
                .eq(ArticleGroup::getId, id)
                .eq(ArticleGroup::getCreatedBy, username));
    }

    protected ArticleGroup findGroupByNameAndOwner(String name, String username) {
        return getOne(new LambdaQueryWrapper<ArticleGroup>()
                .eq(ArticleGroup::getName, name)
                .eq(ArticleGroup::getCreatedBy, username));
    }

    protected long countArticlesByGroupId(Long groupId) {
        return articleGroupRelationMapper.selectCount(new LambdaQueryWrapper<ArticleGroupRelation>()
                .eq(ArticleGroupRelation::getGroupId, groupId));
    }

    protected void deleteRelationsByGroupId(Long groupId) {
        articleGroupRelationMapper.delete(new LambdaQueryWrapper<ArticleGroupRelation>()
                .eq(ArticleGroupRelation::getGroupId, groupId));
    }
}
