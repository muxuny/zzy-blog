package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.blog.common.ArticleStatus;
import com.blog.common.BusinessException;
import com.blog.dto.ArticleRequest;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.entity.User;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.impl.ArticleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArticleServiceImplTest {

    private TestArticleService articleService;

    @BeforeEach
    void setUp() {
        ArticleTagMapper articleTagMapper = mock(ArticleTagMapper.class);
        TagMapper tagMapper = mock(TagMapper.class);
        UserService userService = mock(UserService.class);
        when(articleTagMapper.selectList(any())).thenReturn(java.util.Collections.<ArticleTag>emptyList());
        when(userService.getOne(any(Wrapper.class))).thenReturn(null);
        articleService = new TestArticleService(articleTagMapper, tagMapper, userService);
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

    private static Article article(Long id, String createdBy, String status) {
        Article article = new Article();
        article.setId(id);
        article.setCreatedBy(createdBy);
        article.setTitle("Original title");
        article.setContent("Original content");
        article.setSummary("Original summary");
        article.setStatus(status);
        article.setViewCount(0);
        article.setDeleted(0);
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

    private static class TestArticleService extends ArticleServiceImpl {
        private final Map<Long, Article> articles = new HashMap<>();
        private long nextId = 100L;

        TestArticleService(ArticleTagMapper articleTagMapper, TagMapper tagMapper, UserService userService) {
            super(articleTagMapper, tagMapper, userService);
        }

        void put(Article article) {
            articles.put(article.getId(), article);
        }

        @Override
        public Article getById(Serializable id) {
            return articles.get((Long) id);
        }

        @Override
        public boolean save(Article article) {
            if (article.getId() == null) {
                article.setId(nextId++);
            }
            articles.put(article.getId(), article);
            return true;
        }

        @Override
        public boolean updateById(Article article) {
            articles.put(article.getId(), article);
            return true;
        }

        @Override
        public boolean removeById(Serializable id) {
            articles.remove((Long) id);
            return true;
        }
    }
}
