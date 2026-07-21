package com.blog.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.blog.common.ArticleStatus;
import com.blog.common.ArticleVisibility;
import com.blog.common.BusinessException;
import com.blog.dto.ArticleNeighbors;
import com.blog.dto.ArticlePageQuery;
import com.blog.dto.ArticleRequest;
import com.blog.entity.Article;
import com.blog.entity.ArticleGroup;
import com.blog.entity.ArticleGroupRelation;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.mapper.ArticleGroupMapper;
import com.blog.mapper.ArticleGroupRelationMapper;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.impl.ArticleServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArticleServiceImplTest {

    private TestArticleService articleService;

    @BeforeEach
    void setUp() {
        ArticleTagMapper articleTagMapper = mock(ArticleTagMapper.class);
        TagMapper tagMapper = mock(TagMapper.class);
        UserService userService = mock(UserService.class);
        ArticleGroupMapper articleGroupMapper = mock(ArticleGroupMapper.class);
        ArticleGroupRelationMapper articleGroupRelationMapper = mock(ArticleGroupRelationMapper.class);
        when(articleTagMapper.selectList(any())).thenReturn(java.util.Collections.<ArticleTag>emptyList());
        when(userService.getOne(any(Wrapper.class))).thenReturn(null);
        articleService = new TestArticleService(articleTagMapper, tagMapper, userService,
                articleGroupMapper, articleGroupRelationMapper);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void keywordSearch_shouldApplyTitleOrSummaryFilterToAllArticleLists() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                Article.class);
        ArticleMapper articleMapper = mock(ArticleMapper.class);
        when(articleMapper.selectPage(any(IPage.class), any(Wrapper.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ArticleServiceImpl keywordService = new ArticleServiceImpl(
                mock(ArticleTagMapper.class),
                mock(TagMapper.class),
                mock(UserService.class),
                mock(ArticleGroupMapper.class),
                mock(ArticleGroupRelationMapper.class));
        ReflectionTestUtils.setField(keywordService, "baseMapper", articleMapper);
        ArticlePageQuery query = new ArticlePageQuery();
        query.setKeyword("architecture");
        query.setStatus(ArticleStatus.DRAFT);
        query.setVisibility(ArticleVisibility.PUBLIC);
        query.setAuthor("alice");

        keywordService.getPublicPage(query);
        keywordService.getMyPage(query, "alice");
        keywordService.getAdminPage(query);

        ArgumentCaptor<Wrapper<Article>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(articleMapper, times(3)).selectPage(any(IPage.class), wrapperCaptor.capture());
        List<Wrapper<Article>> wrappers = wrapperCaptor.getAllValues();
        assertThat(wrappers.get(0).getSqlSegment())
                .contains("status =")
                .contains("visibility =")
                .contains("AND (title LIKE")
                .contains("OR summary LIKE");
        assertThat(wrappers.get(1).getSqlSegment())
                .contains("created_by =")
                .contains("status =")
                .contains("visibility =")
                .contains("AND (title LIKE")
                .contains("OR summary LIKE");
        assertThat(wrappers.get(2).getSqlSegment())
                .contains("status =")
                .contains("visibility =")
                .contains("AND (title LIKE")
                .contains("OR summary LIKE")
                .contains("AND created_by =");
    }

    @Test
    void publicDetail_shouldRejectNonPublishedArticle() {
        Article article = article(1L, "alice", ArticleStatus.DRAFT);
        articleService.put(article);

        assertThatThrownBy(() -> articleService.getPublicDetail(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文章不存在");
    }

    @Test
    void publicDetail_shouldRejectPrivatePublishedArticle() {
        Article article = article(1L, "alice", ArticleStatus.PUBLISHED);
        article.setVisibility("private");
        articleService.put(article);

        assertThatThrownBy(() -> articleService.getPublicDetail(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文章不存在");
    }

    @Test
    void getPublicDetail_shouldIncreaseAndPersistViewCount() {
        Article article = article(1L, "alice", ArticleStatus.PUBLISHED);
        article.setViewCount(7);
        articleService.put(article);

        Article detail = articleService.getPublicDetail(1L);

        assertThat(detail.getViewCount()).isEqualTo(8);
        assertThat(articleService.getById(1L).getViewCount()).isEqualTo(8);
        assertThat(articleService.updateByIdCallCount()).isEqualTo(1);
    }

    @Test
    void getPublicArticleSummary_shouldNotIncreaseViewCount() {
        Article article = article(1L, "alice", ArticleStatus.PUBLISHED);
        article.setViewCount(7);
        articleService.put(article);

        Article summary = articleService.getPublicArticleSummary(1L);

        assertThat(summary.getViewCount()).isEqualTo(7);
        assertThat(articleService.getById(1L).getViewCount()).isEqualTo(7);
        assertThat(articleService.updateByIdCallCount()).isZero();
    }

    @Test
    void getPublicArticleSummaries_shouldFilterUnavailableArticles() {
        Article visible = article(1L, "alice", ArticleStatus.PUBLISHED);
        Article privateArticle = article(2L, "alice", ArticleStatus.PUBLISHED);
        privateArticle.setVisibility(ArticleVisibility.PRIVATE);
        articleService.put(visible);
        articleService.put(privateArticle);
        articleService.put(article(3L, "alice", ArticleStatus.DRAFT));

        List<Article> summaries = articleService.getPublicArticleSummaries(Arrays.asList(1L, 2L, 3L));

        assertThat(summaries).extracting(Article::getId).containsExactly(1L);
    }

    @Test
    void getPublicArticleSummaries_shouldBatchTagsAndAuthorsForAllSummaries() {
        ArticleTagMapper articleTagMapper = mock(ArticleTagMapper.class);
        TagMapper tagMapper = mock(TagMapper.class);
        UserService userService = mock(UserService.class);
        ArticleServiceImpl summaryService = new TestArticleService(articleTagMapper, tagMapper, userService,
                mock(ArticleGroupMapper.class), mock(ArticleGroupRelationMapper.class));
        Article first = article(1L, "alice", ArticleStatus.PUBLISHED);
        first.setTitle("First");
        Article second = article(2L, "bob", ArticleStatus.PUBLISHED);
        second.setTitle("Second");
        ((TestArticleService) summaryService).put(first);
        ((TestArticleService) summaryService).put(second);
        when(articleTagMapper.selectList(any())).thenReturn(Arrays.asList(
                articleTag(1L, 10L), articleTag(1L, 11L), articleTag(2L, 11L)));
        when(tagMapper.selectBatchIds(any())).thenReturn(Arrays.asList(
                tag(10L, "Java"), tag(11L, "Spring")));
        when(userService.list(any())).thenReturn(Arrays.asList(
                user("alice", "Alice"), user("bob", "Bob")));

        List<Article> summaries = summaryService.getPublicArticleSummaries(Arrays.asList(1L, 2L));

        verify(articleTagMapper, times(1)).selectList(any());
        verify(tagMapper, times(1)).selectBatchIds(any());
        verify(userService, times(1)).list(any());
        verify(userService, never()).getOne(any(Wrapper.class));
        assertThat(summaries).extracting(Article::getId).containsExactlyInAnyOrder(1L, 2L);
        assertThat(summaries.stream().filter(article -> article.getId().equals(1L)).findFirst().get().getTags())
                .extracting(Tag::getName).containsExactly("Java", "Spring");
        assertThat(summaries.stream().filter(article -> article.getId().equals(2L)).findFirst().get().getTags())
                .extracting(Tag::getName).containsExactly("Spring");
        assertThat(summaries.stream().filter(article -> article.getId().equals(1L)).findFirst().get()
                .getAuthorName()).isEqualTo("Alice");
        assertThat(summaries.stream().filter(article -> article.getId().equals(2L)).findFirst().get()
                .getAuthorName()).isEqualTo("Bob");
    }

    @Test
    void updateMyArticle_shouldRejectOtherUsersArticle() {
        Article article = article(1L, "alice", ArticleStatus.DRAFT);
        articleService.put(article);

        assertThatThrownBy(() -> articleService.updateMyArticle(1L, request("New title", ArticleStatus.DRAFT), "bob"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无权操作该文章");
    }

    @Test
    void updateMyPublishedArticle_shouldReturnToPending() {
        Article article = article(1L, "alice", ArticleStatus.PUBLISHED);
        articleService.put(article);

        Article updated = articleService.updateMyArticle(1L, request("Updated title", ArticleStatus.PUBLISHED), "alice");

        assertThat(updated.getTitle()).isEqualTo("Updated title");
        assertThat(updated.getStatus()).isEqualTo(ArticleStatus.PENDING);
        assertThat(articleService.getById(1L).getStatus()).isEqualTo(ArticleStatus.PENDING);
    }

    @Test
    void updateMyPendingArticle_shouldRequireWithdrawFirst() {
        Article article = article(1L, "alice", ArticleStatus.PENDING);
        articleService.put(article);

        assertThatThrownBy(() -> articleService.updateMyArticle(1L, request("Updated title", ArticleStatus.DRAFT), "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("待审核文章请先撤回再编辑");
    }

    @Test
    void approveArticle_shouldPublishPendingArticle() {
        Article article = article(1L, "alice", ArticleStatus.PENDING);
        article.setReviewReason("too short");
        articleService.put(article);

        Article approved = articleService.approveArticle(1L);

        assertThat(approved.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(approved.getReviewReason()).isNull();
        assertThat(articleService.getById(1L).getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
    }

    @Test
    void rejectArticle_shouldRequirePendingArticle() {
        Article article = article(1L, "alice", ArticleStatus.DRAFT);
        articleService.put(article);

        assertThatThrownBy(() -> articleService.rejectArticle(1L, "needs work"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("只有待审核文章可以驳回");
    }

    @Test
    void getPublicNeighbors_shouldReturnClosestPublishedArticlesByCreatedAtAndId() {
        articleService.put(article(1L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 1, 10, 0)));
        articleService.put(article(2L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 2, 10, 0)));
        articleService.put(article(3L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 2, 10, 0)));
        articleService.put(article(4L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 3, 10, 0)));
        articleService.put(article(5L, "alice", ArticleStatus.DRAFT, LocalDateTime.of(2024, 1, 4, 10, 0)));
        articleService.put(article(6L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 4, 10, 0), 1));

        ArticleNeighbors neighbors = articleService.getPublicNeighbors(3L);

        assertThat(neighbors.getPrevious().getId()).isEqualTo(2L);
        assertThat(neighbors.getNext().getId()).isEqualTo(4L);
    }

    @Test
    void getPublicNeighbors_shouldRejectNonPublicCurrentArticle() {
        articleService.put(article(1L, "alice", ArticleStatus.DRAFT, LocalDateTime.of(2024, 1, 1, 10, 0)));

        assertThatThrownBy(() -> articleService.getPublicNeighbors(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文章不存在");
    }

    @Test
    void getRelatedArticles_shouldSortBySharedTagsThenCreatedAtThenIdAndCapSize() {
        articleService.put(article(1L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 10, 10, 0)));
        articleService.put(article(2L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 9, 10, 0)));
        articleService.put(article(3L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 11, 10, 0)));
        articleService.put(article(4L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 8, 10, 0)));
        articleService.put(article(5L, "alice", ArticleStatus.DRAFT, LocalDateTime.of(2024, 1, 12, 10, 0)));
        articleService.put(article(6L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 11, 10, 0)));
        articleService.put(article(7L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 7, 10, 0)));
        articleService.put(article(8L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 6, 10, 0)));
        articleService.put(article(9L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 5, 10, 0)));
        articleService.put(article(10L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 4, 10, 0)));
        articleService.put(article(11L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 3, 10, 0)));
        articleService.put(article(12L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 2, 10, 0)));
        articleService.put(article(13L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 1, 10, 0)));
        articleService.put(article(14L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2023, 12, 31, 10, 0)));
        articleService.put(article(15L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2023, 12, 30, 10, 0)));
        articleService.linkTags(1L, 1L, 2L, 3L);
        articleService.linkTags(2L, 1L, 2L);
        articleService.linkTags(3L, 1L);
        articleService.linkTags(4L, 2L, 3L);
        articleService.linkTags(5L, 1L, 2L, 3L);
        articleService.linkTags(6L, 3L);
        articleService.linkTags(7L, 1L);
        articleService.linkTags(8L, 1L);
        articleService.linkTags(9L, 1L);
        articleService.linkTags(10L, 1L);
        articleService.linkTags(11L, 1L);
        articleService.linkTags(12L, 1L);
        articleService.linkTags(13L, 1L);
        articleService.linkTags(14L, 1L);
        articleService.linkTags(15L, 1L);

        List<Article> related = articleService.getRelatedArticles(1L, 20);

        assertThat(related).extracting(Article::getId)
                .containsExactly(2L, 4L, 6L, 3L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L);
    }

    @Test
    void createMyArticle_shouldSaveMultipleOwnedGroupsAndAttachThem() {
        articleService.putGroup(group(10L, "alice", "Ideas"));
        articleService.putGroup(group(11L, "alice", "Drafts"));
        ArticleRequest request = request("Grouped article", ArticleStatus.DRAFT);
        request.setGroupIds(Arrays.asList(10L, 11L));

        Article created = articleService.createMyArticle(request, "alice");

        assertThat(articleService.groupRelations()).extracting(ArticleGroupRelation::getArticleId)
                .containsExactly(created.getId(), created.getId());
        assertThat(articleService.groupRelations()).extracting(ArticleGroupRelation::getGroupId)
                .containsExactly(10L, 11L);
        assertThat(created.getGroups()).extracting(ArticleGroup::getId)
                .containsExactly(10L, 11L);
    }

    @Test
    void createMyArticle_shouldSaveVisibilityPreference() {
        ArticleRequest request = request("Private note", ArticleStatus.DRAFT);
        request.setVisibility("private");

        Article created = articleService.createMyArticle(request, "alice");

        assertThat(created.getVisibility()).isEqualTo("private");
    }

    @Test
    void createMyArticle_shouldRejectMissingOrOtherUsersGroup() {
        articleService.putGroup(group(10L, "bob", "Bob group"));
        ArticleRequest request = request("Grouped article", ArticleStatus.DRAFT);
        request.setGroupIds(Arrays.asList(10L));

        assertThatThrownBy(() -> articleService.createMyArticle(request, "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分组不存在");
    }

    @Test
    void updateMyArticle_shouldClearGroupsWhenRequestHasNoGroups() {
        articleService.put(article(1L, "alice", ArticleStatus.DRAFT));
        articleService.putGroup(group(10L, "alice", "Ideas"));
        articleService.linkGroup(1L, 10L);
        ArticleRequest request = request("Updated title", ArticleStatus.DRAFT);
        request.setGroupIds(java.util.Collections.emptyList());

        Article updated = articleService.updateMyArticle(1L, request, "alice");

        assertThat(articleService.groupRelations()).isEmpty();
        assertThat(updated.getGroups()).isEmpty();
    }

    @Test
    void updateMyArticle_shouldPreserveExistingVisibilityWhenRequestOmitsVisibility() {
        Article article = article(1L, "alice", ArticleStatus.DRAFT);
        article.setVisibility(ArticleVisibility.PRIVATE);
        articleService.put(article);

        Article updated = articleService.updateMyArticle(1L, request("Updated title", ArticleStatus.DRAFT), "alice");

        assertThat(updated.getVisibility()).isEqualTo(ArticleVisibility.PRIVATE);
    }

    @Test
    void updateAdminArticle_shouldPreserveExistingVisibilityWhenRequestOmitsVisibility() {
        Article article = article(1L, "alice", ArticleStatus.PUBLISHED);
        article.setVisibility(ArticleVisibility.PRIVATE);
        articleService.put(article);

        Article updated = articleService.updateAdminArticle(1L, request("Admin updated title", ArticleStatus.PUBLISHED));

        assertThat(updated.getVisibility()).isEqualTo(ArticleVisibility.PRIVATE);
    }

    @Test
    void updateMyArticleGroups_shouldKeepPublishedArticlePublished() {
        articleService.put(article(1L, "alice", ArticleStatus.PUBLISHED));
        articleService.putGroup(group(10L, "alice", "Published"));

        Article updated = articleService.updateMyArticleGroups(1L, Arrays.asList(10L), "alice");

        assertThat(updated.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(articleService.getById(1L).getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(updated.getGroups()).extracting(ArticleGroup::getId).containsExactly(10L);
    }

    @Test
    void updateMyArticleGroups_shouldClearGroupsWithoutChangingStatus() {
        articleService.put(article(1L, "alice", ArticleStatus.PUBLISHED));
        articleService.putGroup(group(10L, "alice", "Published"));
        articleService.linkGroup(1L, 10L);

        Article updated = articleService.updateMyArticleGroups(1L, java.util.Collections.emptyList(), "alice");

        assertThat(updated.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(articleService.groupRelations()).isEmpty();
        assertThat(updated.getGroups()).isEmpty();
    }

    @Test
    void updateMyArticleGroups_shouldRejectOtherUsersGroup() {
        articleService.put(article(1L, "alice", ArticleStatus.PUBLISHED));
        articleService.putGroup(group(20L, "bob", "Bob group"));

        assertThatThrownBy(() -> articleService.updateMyArticleGroups(1L, Arrays.asList(20L), "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分组不存在");
    }

    @Test
    void updateMyArticleVisibility_shouldChangeVisibilityWithoutChangingStatus() {
        Article article = article(1L, "alice", ArticleStatus.PUBLISHED);
        article.setVisibility("public");
        articleService.put(article);

        Article updated = articleService.updateMyArticleVisibility(1L, "private", "alice");

        assertThat(updated.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(updated.getVisibility()).isEqualTo("private");
        assertThat(articleService.getById(1L).getVisibility()).isEqualTo("private");
    }

    @Test
    void updateMyArticleVisibility_shouldRejectInvalidVisibility() {
        articleService.put(article(1L, "alice", ArticleStatus.DRAFT));

        assertThatThrownBy(() -> articleService.updateMyArticleVisibility(1L, "friends", "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文章可见性不合法");
    }

    @Test
    void updateMyArticleVisibility_shouldRejectBlankVisibility() {
        Article article = article(1L, "alice", ArticleStatus.PUBLISHED);
        article.setVisibility(ArticleVisibility.PRIVATE);
        articleService.put(article);

        assertThatThrownBy(() -> articleService.updateMyArticleVisibility(1L, "", "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文章可见性不合法");
        assertThat(articleService.getById(1L).getVisibility()).isEqualTo(ArticleVisibility.PRIVATE);
    }

    @Test
    void getMyPage_shouldFilterByGroupAndAttachGroups() {
        articleService.put(article(1L, "alice", ArticleStatus.DRAFT, LocalDateTime.of(2024, 1, 1, 10, 0)));
        articleService.put(article(2L, "alice", ArticleStatus.DRAFT, LocalDateTime.of(2024, 1, 2, 10, 0)));
        articleService.put(article(3L, "bob", ArticleStatus.DRAFT, LocalDateTime.of(2024, 1, 3, 10, 0)));
        articleService.putGroup(group(10L, "alice", "Ideas"));
        articleService.putGroup(group(20L, "bob", "Bob group"));
        articleService.linkGroup(2L, 10L);
        articleService.linkGroup(3L, 20L);
        ArticlePageQuery query = new ArticlePageQuery();
        query.setGroupId(10L);

        List<Article> records = articleService.getMyPage(query, "alice").getRecords();

        assertThat(records).extracting(Article::getId).containsExactly(2L);
        assertThat(records.get(0).getGroups()).extracting(ArticleGroup::getId).containsExactly(10L);
    }

    @Test
    void getMyPage_shouldReturnOnlyUngroupedArticlesForCurrentUser() {
        articleService.put(article(1L, "alice", ArticleStatus.DRAFT, LocalDateTime.of(2024, 1, 1, 10, 0)));
        articleService.put(article(2L, "alice", ArticleStatus.DRAFT, LocalDateTime.of(2024, 1, 2, 10, 0)));
        articleService.put(article(3L, "bob", ArticleStatus.DRAFT, LocalDateTime.of(2024, 1, 3, 10, 0)));
        articleService.putGroup(group(10L, "alice", "Ideas"));
        articleService.putGroup(group(20L, "bob", "Bob group"));
        articleService.linkGroup(2L, 10L);
        articleService.linkGroup(3L, 20L);
        ArticlePageQuery query = new ArticlePageQuery();
        query.setUngrouped(true);

        List<Article> records = articleService.getMyPage(query, "alice").getRecords();

        assertThat(records).extracting(Article::getId).containsExactly(1L);
        assertThat(records.get(0).getGroups()).isEmpty();
    }

    @Test
    void getMyPage_shouldFilterByVisibilityForCurrentUser() {
        Article publicArticle = article(1L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 1, 10, 0));
        publicArticle.setVisibility(ArticleVisibility.PUBLIC);
        Article privateArticle = article(2L, "alice", ArticleStatus.PUBLISHED, LocalDateTime.of(2024, 1, 2, 10, 0));
        privateArticle.setVisibility(ArticleVisibility.PRIVATE);
        articleService.put(publicArticle);
        articleService.put(privateArticle);
        ArticlePageQuery query = new ArticlePageQuery();
        query.setVisibility(ArticleVisibility.PRIVATE);

        List<Article> records = articleService.getMyPage(query, "alice").getRecords();

        assertThat(records).extracting(Article::getId).containsExactly(2L);
    }

    @Test
    void getMyPage_shouldTreatNullVisibilityAsPublicWhenFilteringPublic() {
        Article legacyPublicArticle = article(1L, "alice", ArticleStatus.PUBLISHED,
                LocalDateTime.of(2024, 1, 1, 10, 0));
        Article explicitPublicArticle = article(2L, "alice", ArticleStatus.PUBLISHED,
                LocalDateTime.of(2024, 1, 2, 10, 0));
        explicitPublicArticle.setVisibility(ArticleVisibility.PUBLIC);
        Article privateArticle = article(3L, "alice", ArticleStatus.PUBLISHED,
                LocalDateTime.of(2024, 1, 3, 10, 0));
        privateArticle.setVisibility(ArticleVisibility.PRIVATE);
        articleService.put(legacyPublicArticle);
        articleService.put(explicitPublicArticle);
        articleService.put(privateArticle);
        ArticlePageQuery query = new ArticlePageQuery();
        query.setVisibility(ArticleVisibility.PUBLIC);

        List<Article> records = articleService.getMyPage(query, "alice").getRecords();

        assertThat(records).extracting(Article::getId).containsExactly(2L, 1L);
    }

    private static Article article(Long id, String createdBy, String status) {
        return article(id, createdBy, status, null);
    }

    private static Article article(Long id, String createdBy, String status, LocalDateTime createdAt) {
        return article(id, createdBy, status, createdAt, 0);
    }

    private static Article article(Long id, String createdBy, String status, LocalDateTime createdAt, Integer deleted) {
        Article article = new Article();
        article.setId(id);
        article.setCreatedBy(createdBy);
        article.setTitle("Original title");
        article.setContent("Original content");
        article.setSummary("Original summary");
        article.setStatus(status);
        article.setViewCount(0);
        article.setDeleted(deleted);
        article.setCreatedAt(createdAt);
        return article;
    }

    private static ArticleRequest request(String title, String status) {
        ArticleRequest request = new ArticleRequest();
        request.setTitle(title);
        request.setContent("Updated content");
        request.setSummary("Updated summary");
        request.setCoverImage("cover.png");
        request.setStatus(status);
        return request;
    }

    private static ArticleGroup group(Long id, String createdBy, String name) {
        ArticleGroup group = new ArticleGroup();
        group.setId(id);
        group.setCreatedBy(createdBy);
        group.setName(name);
        group.setDeleted(0);
        return group;
    }

    private static ArticleTag articleTag(Long articleId, Long tagId) {
        ArticleTag articleTag = new ArticleTag();
        articleTag.setArticleId(articleId);
        articleTag.setTagId(tagId);
        return articleTag;
    }

    private static Tag tag(Long id, String name) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tag;
    }

    private static User user(String username, String nickname) {
        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        return user;
    }

    private static class TestArticleService extends ArticleServiceImpl {
        private final Map<Long, Article> articles = new HashMap<>();
        private final List<ArticleTag> articleTags = new ArrayList<>();
        private final Map<Long, ArticleGroup> groups = new HashMap<>();
        private final List<ArticleGroupRelation> articleGroupRelations = new ArrayList<>();
        private long nextId = 100L;
        private int updateByIdCallCount;

        TestArticleService(ArticleTagMapper articleTagMapper,
                           TagMapper tagMapper,
                           UserService userService,
                           ArticleGroupMapper articleGroupMapper,
                           ArticleGroupRelationMapper articleGroupRelationMapper) {
            super(articleTagMapper, tagMapper, userService, articleGroupMapper, articleGroupRelationMapper);
        }

        void put(Article article) {
            articles.put(article.getId(), copyArticle(article));
        }

        int updateByIdCallCount() {
            return updateByIdCallCount;
        }

        void linkTags(Long articleId, Long... tagIds) {
            for (Long tagId : tagIds) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(articleId);
                articleTag.setTagId(tagId);
                articleTags.add(articleTag);
            }
        }

        void putGroup(ArticleGroup group) {
            groups.put(group.getId(), group);
        }

        void linkGroup(Long articleId, Long groupId) {
            ArticleGroupRelation relation = new ArticleGroupRelation();
            relation.setArticleId(articleId);
            relation.setGroupId(groupId);
            articleGroupRelations.add(relation);
        }

        List<ArticleGroupRelation> groupRelations() {
            return articleGroupRelations;
        }

        @Override
        public Article getById(Serializable id) {
            return copyArticle(articles.get((Long) id));
        }

        @Override
        public boolean save(Article article) {
            if (article.getId() == null) {
                article.setId(nextId++);
            }
            articles.put(article.getId(), copyArticle(article));
            return true;
        }

        @Override
        public boolean updateById(Article article) {
            updateByIdCallCount++;
            articles.put(article.getId(), copyArticle(article));
            return true;
        }

        @Override
        public boolean removeById(Serializable id) {
            articles.remove((Long) id);
            return true;
        }

        @Override
        protected Article findPreviousPublicArticle(Article current) {
            return articles.values().stream()
                    .filter(article -> ArticleStatus.isPublic(article.getStatus()))
                    .filter(article -> ArticleVisibility.isPublic(article.getVisibility()))
                    .filter(article -> article.getDeleted() == null || article.getDeleted() == 0)
                    .filter(article -> isBefore(article, current))
                    .max(Comparator.comparing(Article::getCreatedAt).thenComparing(Article::getId))
                    .orElse(null);
        }

        @Override
        protected Article findNextPublicArticle(Article current) {
            return articles.values().stream()
                    .filter(article -> ArticleStatus.isPublic(article.getStatus()))
                    .filter(article -> ArticleVisibility.isPublic(article.getVisibility()))
                    .filter(article -> article.getDeleted() == null || article.getDeleted() == 0)
                    .filter(article -> isBefore(current, article))
                    .min(Comparator.comparing(Article::getCreatedAt).thenComparing(Article::getId))
                    .orElse(null);
        }

        private boolean isBefore(Article left, Article right) {
            int createdAtCompare = left.getCreatedAt().compareTo(right.getCreatedAt());
            if (createdAtCompare != 0) {
                return createdAtCompare < 0;
            }
            return left.getId().compareTo(right.getId()) < 0;
        }

        @Override
        protected List<ArticleTag> listArticleTagsByArticleId(Long articleId) {
            return articleTags.stream()
                    .filter(articleTag -> articleTag.getArticleId().equals(articleId))
                    .collect(Collectors.toList());
        }

        @Override
        protected List<ArticleTag> listArticleTagsByTagIds(Collection<Long> tagIds) {
            return articleTags.stream()
                    .filter(articleTag -> tagIds.contains(articleTag.getTagId()))
                    .collect(Collectors.toList());
        }

        @Override
        protected List<Article> listPublicArticlesByIds(Collection<Long> articleIds) {
            return articles.values().stream()
                    .filter(article -> articleIds.contains(article.getId()))
                    .filter(article -> ArticleStatus.isPublic(article.getStatus()))
                    .filter(article -> ArticleVisibility.isPublic(article.getVisibility()))
                    .filter(article -> article.getDeleted() == null || article.getDeleted() == 0)
                    .collect(Collectors.toList());
        }

        @Override
        protected List<Article> listPublicArticleSummariesByIds(Collection<Long> articleIds) {
            return listPublicArticlesByIds(articleIds);
        }

        @Override
        protected List<ArticleGroup> listArticleGroupsByIds(Collection<Long> groupIds, String username) {
            return groups.values().stream()
                    .filter(group -> groupIds.contains(group.getId()))
                    .filter(group -> username.equals(group.getCreatedBy()))
                    .filter(group -> group.getDeleted() == null || group.getDeleted() == 0)
                    .collect(Collectors.toList());
        }

        @Override
        protected ArticleGroup findArticleGroupById(Long groupId, String username) {
            ArticleGroup group = groups.get(groupId);
            if (group == null || !username.equals(group.getCreatedBy())
                    || group.getDeleted() != null && group.getDeleted() == 1) {
                return null;
            }
            return group;
        }

        @Override
        protected List<ArticleGroup> listArticleGroupsByOwner(String username) {
            return groups.values().stream()
                    .filter(group -> username.equals(group.getCreatedBy()))
                    .filter(group -> group.getDeleted() == null || group.getDeleted() == 0)
                    .collect(Collectors.toList());
        }

        @Override
        protected List<ArticleGroup> listArticleGroupsByArticleId(Long articleId) {
            List<Long> groupIds = articleGroupRelations.stream()
                    .filter(relation -> articleId.equals(relation.getArticleId()))
                    .map(ArticleGroupRelation::getGroupId)
                    .collect(Collectors.toList());
            return groups.values().stream()
                    .filter(group -> groupIds.contains(group.getId()))
                    .collect(Collectors.toList());
        }

        @Override
        protected List<ArticleGroupRelation> listArticleGroupRelationsByGroupId(Long groupId) {
            return articleGroupRelations.stream()
                    .filter(relation -> groupId.equals(relation.getGroupId()))
                    .collect(Collectors.toList());
        }

        @Override
        protected List<ArticleGroupRelation> listArticleGroupRelationsByGroupIds(Collection<Long> groupIds) {
            return articleGroupRelations.stream()
                    .filter(relation -> groupIds.contains(relation.getGroupId()))
                    .collect(Collectors.toList());
        }

        @Override
        protected void deleteArticleGroupRelationsByArticleId(Long articleId) {
            articleGroupRelations.removeIf(relation -> articleId.equals(relation.getArticleId()));
        }

        @Override
        protected void insertArticleGroupRelation(ArticleGroupRelation relation) {
            articleGroupRelations.add(relation);
        }

        @Override
        protected com.baomidou.mybatisplus.core.metadata.IPage<Article> selectMyArticlePage(
                com.baomidou.mybatisplus.extension.plugins.pagination.Page<Article> page,
                com.blog.dto.ArticlePageQuery query,
                String username,
                Collection<Long> includeArticleIds,
                Collection<Long> excludeArticleIds) {
            List<Article> records = articles.values().stream()
                    .filter(article -> username.equals(article.getCreatedBy()))
                    .filter(article -> article.getDeleted() == null || article.getDeleted() == 0)
                    .filter(article -> query.getStatus() == null || query.getStatus().isEmpty()
                            || query.getStatus().equals(article.getStatus()))
                    .filter(article -> matchesVisibility(query.getVisibility(), article.getVisibility()))
                    .filter(article -> query.getKeyword() == null || query.getKeyword().isEmpty()
                            || article.getTitle().contains(query.getKeyword())
                            || article.getSummary() != null && article.getSummary().contains(query.getKeyword()))
                    .filter(article -> includeArticleIds == null || includeArticleIds.contains(article.getId()))
                    .filter(article -> excludeArticleIds == null || !excludeArticleIds.contains(article.getId()))
                    .sorted(Comparator.comparing(Article::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
            page.setRecords(records);
            page.setTotal(records.size());
            return page;
        }

        private boolean matchesVisibility(String requestedVisibility, String articleVisibility) {
            if (requestedVisibility == null || requestedVisibility.isEmpty()) {
                return true;
            }
            if (ArticleVisibility.PUBLIC.equals(requestedVisibility)) {
                return ArticleVisibility.isPublic(articleVisibility);
            }
            return requestedVisibility.equals(articleVisibility);
        }

        private Article copyArticle(Article article) {
            if (article == null) {
                return null;
            }
            Article copy = new Article();
            BeanUtils.copyProperties(article, copy);
            return copy;
        }
    }
}
