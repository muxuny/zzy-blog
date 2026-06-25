package com.blog.service;

import com.blog.common.BusinessException;
import com.blog.dto.ArticleGroupRequest;
import com.blog.dto.ArticleGroupSummary;
import com.blog.entity.ArticleGroup;
import com.blog.entity.ArticleGroupRelation;
import com.blog.mapper.ArticleGroupMapper;
import com.blog.mapper.ArticleGroupRelationMapper;
import com.blog.service.impl.ArticleGroupServiceImpl;
import org.apache.ibatis.exceptions.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ArticleGroupServiceImplTest {

    private TestArticleGroupService articleGroupService;

    @BeforeEach
    void setUp() {
        articleGroupService = new TestArticleGroupService(
                mock(ArticleGroupMapper.class),
                mock(ArticleGroupRelationMapper.class));
    }

    @Test
    void listMyGroups_shouldReturnOwnedGroupsWithArticleCountsInStableOrder() {
        articleGroupService.put(group(10L, "alice", "Later", LocalDateTime.of(2024, 1, 2, 10, 0)));
        articleGroupService.put(group(11L, "alice", "Ideas", LocalDateTime.of(2024, 1, 1, 10, 0)));
        articleGroupService.put(group(12L, "bob", "Bob group", LocalDateTime.of(2024, 1, 1, 9, 0)));
        articleGroupService.link(101L, 10L);
        articleGroupService.link(102L, 10L);
        articleGroupService.link(103L, 11L);
        articleGroupService.link(104L, 12L);

        List<ArticleGroupSummary> summaries = articleGroupService.listMyGroups("alice");

        assertThat(summaries).extracting(ArticleGroupSummary::getId)
                .containsExactly(11L, 10L);
        assertThat(summaries).extracting(ArticleGroupSummary::getArticleCount)
                .containsExactly(1L, 2L);
    }

    @Test
    void createMyGroup_shouldTrimNameAndSetOwner() {
        ArticleGroupRequest request = request("  Ideas  ");

        ArticleGroup created = articleGroupService.createMyGroup(request, "alice");

        assertThat(created.getName()).isEqualTo("Ideas");
        assertThat(created.getCreatedBy()).isEqualTo("alice");
    }

    @Test
    void createMyGroup_shouldRejectBlankName() {
        assertThatThrownBy(() -> articleGroupService.createMyGroup(request("   "), "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分组名称不能为空");
    }

    @Test
    void createMyGroup_shouldRejectDuplicateNameForSameOwner() {
        articleGroupService.put(group(10L, "alice", "Ideas", LocalDateTime.of(2024, 1, 1, 10, 0)));

        assertThatThrownBy(() -> articleGroupService.createMyGroup(request("Ideas"), "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分组已存在");
    }

    @Test
    void createMyGroup_shouldConvertDuplicateKeyToBusinessMessage() {
        articleGroupService.failNextSaveWithDuplicateKey();

        assertThatThrownBy(() -> articleGroupService.createMyGroup(request("Ideas"), "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分组已存在");
    }

    @Test
    void updateMyGroup_shouldOnlyUpdateOwnedGroupAndRejectDuplicateName() {
        articleGroupService.put(group(10L, "alice", "Ideas", LocalDateTime.of(2024, 1, 1, 10, 0)));
        articleGroupService.put(group(11L, "alice", "Drafts", LocalDateTime.of(2024, 1, 2, 10, 0)));
        articleGroupService.put(group(12L, "bob", "Bob group", LocalDateTime.of(2024, 1, 3, 10, 0)));

        assertThatThrownBy(() -> articleGroupService.updateMyGroup(12L, request("Other"), "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分组不存在");
        assertThatThrownBy(() -> articleGroupService.updateMyGroup(10L, request("Drafts"), "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分组已存在");

        ArticleGroup updated = articleGroupService.updateMyGroup(10L, request("  Later  "), "alice");

        assertThat(updated.getName()).isEqualTo("Later");
        assertThat(articleGroupService.getById(10L).getName()).isEqualTo("Later");
    }

    @Test
    void updateMyGroup_shouldConvertDuplicateKeyToBusinessMessage() {
        articleGroupService.put(group(10L, "alice", "Ideas", LocalDateTime.of(2024, 1, 1, 10, 0)));
        articleGroupService.failNextUpdateWithDuplicateKey();

        assertThatThrownBy(() -> articleGroupService.updateMyGroup(10L, request("Later"), "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分组已存在");
    }

    @Test
    void deleteMyGroup_shouldDeleteOnlyOwnedGroupAndRemoveRelations() {
        articleGroupService.put(group(10L, "alice", "Ideas", LocalDateTime.of(2024, 1, 1, 10, 0)));
        articleGroupService.put(group(12L, "bob", "Bob group", LocalDateTime.of(2024, 1, 3, 10, 0)));
        articleGroupService.link(101L, 10L);
        articleGroupService.link(102L, 10L);
        articleGroupService.link(104L, 12L);

        articleGroupService.deleteMyGroup(10L, "alice");

        assertThat(articleGroupService.getById(10L)).isNull();
        assertThat(articleGroupService.relations()).extracting(ArticleGroupRelation::getGroupId)
                .containsExactly(12L);
        assertThatThrownBy(() -> articleGroupService.deleteMyGroup(12L, "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分组不存在");
    }

    private static ArticleGroupRequest request(String name) {
        ArticleGroupRequest request = new ArticleGroupRequest();
        request.setName(name);
        return request;
    }

    private static ArticleGroup group(Long id, String createdBy, String name, LocalDateTime createdAt) {
        ArticleGroup group = new ArticleGroup();
        group.setId(id);
        group.setCreatedBy(createdBy);
        group.setName(name);
        group.setCreatedAt(createdAt);
        group.setDeleted(0);
        return group;
    }

    private static class TestArticleGroupService extends ArticleGroupServiceImpl {
        private final Map<Long, ArticleGroup> groups = new HashMap<>();
        private final List<ArticleGroupRelation> relations = new ArrayList<>();
        private long nextId = 100L;
        private boolean failNextSaveWithDuplicateKey;
        private boolean failNextUpdateWithDuplicateKey;

        TestArticleGroupService(ArticleGroupMapper articleGroupMapper,
                                ArticleGroupRelationMapper articleGroupRelationMapper) {
            super(articleGroupMapper, articleGroupRelationMapper);
        }

        void put(ArticleGroup group) {
            groups.put(group.getId(), group);
        }

        void link(Long articleId, Long groupId) {
            ArticleGroupRelation relation = new ArticleGroupRelation();
            relation.setArticleId(articleId);
            relation.setGroupId(groupId);
            relations.add(relation);
        }

        void failNextSaveWithDuplicateKey() {
            failNextSaveWithDuplicateKey = true;
        }

        void failNextUpdateWithDuplicateKey() {
            failNextUpdateWithDuplicateKey = true;
        }

        List<ArticleGroupRelation> relations() {
            return relations;
        }

        @Override
        public ArticleGroup getById(Serializable id) {
            return groups.get((Long) id);
        }

        @Override
        public boolean save(ArticleGroup group) {
            if (failNextSaveWithDuplicateKey) {
                failNextSaveWithDuplicateKey = false;
                throw new DuplicateKeyException("duplicate group", new PersistenceException("duplicate"));
            }
            if (group.getId() == null) {
                group.setId(nextId++);
            }
            groups.put(group.getId(), group);
            return true;
        }

        @Override
        public boolean updateById(ArticleGroup group) {
            if (failNextUpdateWithDuplicateKey) {
                failNextUpdateWithDuplicateKey = false;
                throw new DuplicateKeyException("duplicate group", new PersistenceException("duplicate"));
            }
            groups.put(group.getId(), group);
            return true;
        }

        @Override
        public boolean removeById(Serializable id) {
            groups.remove((Long) id);
            return true;
        }

        @Override
        protected List<ArticleGroup> listGroupsByOwner(String username) {
            return groups.values().stream()
                    .filter(group -> username.equals(group.getCreatedBy()))
                    .filter(group -> group.getDeleted() == null || group.getDeleted() == 0)
                    .sorted(Comparator
                            .comparing(ArticleGroup::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(ArticleGroup::getId))
                    .collect(Collectors.toList());
        }

        @Override
        protected ArticleGroup findGroupByIdAndOwner(Long id, String username) {
            ArticleGroup group = groups.get(id);
            if (group == null || !username.equals(group.getCreatedBy())
                    || group.getDeleted() != null && group.getDeleted() == 1) {
                return null;
            }
            return group;
        }

        @Override
        protected ArticleGroup findGroupByNameAndOwner(String name, String username) {
            return groups.values().stream()
                    .filter(group -> username.equals(group.getCreatedBy()))
                    .filter(group -> name.equals(group.getName()))
                    .filter(group -> group.getDeleted() == null || group.getDeleted() == 0)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        protected long countArticlesByGroupId(Long groupId) {
            return relations.stream()
                    .filter(relation -> groupId.equals(relation.getGroupId()))
                    .count();
        }

        @Override
        protected void deleteRelationsByGroupId(Long groupId) {
            relations.removeIf(relation -> groupId.equals(relation.getGroupId()));
        }
    }
}
