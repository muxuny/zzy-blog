package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.ArticleStatus;
import com.blog.common.BusinessException;
import com.blog.dto.ArticlePageQuery;
import com.blog.dto.ArticleRequest;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.ArticleService;
import com.blog.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;
    private final UserService userService;

    public ArticleServiceImpl(ArticleTagMapper articleTagMapper,
                              TagMapper tagMapper,
                              UserService userService) {
        this.articleTagMapper = articleTagMapper;
        this.tagMapper = tagMapper;
        this.userService = userService;
    }

    @Override
    public IPage<Article> getPublicPage(ArticlePageQuery query) {
        Page<Article> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, ArticleStatus.PUBLISHED)
                .orderByDesc(Article::getCreatedAt);

        if (query.getTagId() != null) {
            List<Long> articleIds = articleTagMapper.selectList(
                    new LambdaQueryWrapper<ArticleTag>()
                            .eq(ArticleTag::getTagId, query.getTagId()))
                    .stream().map(ArticleTag::getArticleId).collect(Collectors.toList());
            if (articleIds.isEmpty()) {
                return page;
            }
            wrapper.in(Article::getId, articleIds);
        }
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(Article::getTitle, query.getKeyword());
        }

        IPage<Article> result = baseMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::attachTagsAndAuthor);
        return result;
    }

    @Override
    public Article getPublicDetail(Long id) {
        Article article = requireArticle(id);
        if (!ArticleStatus.isPublic(article.getStatus())) {
            throw new BusinessException("文章不存在");
        }
        article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
        updateById(article);
        attachTagsAndAuthor(article);
        return article;
    }

    @Override
    public IPage<Article> getMyPage(ArticlePageQuery query, String username) {
        Page<Article> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getCreatedBy, username)
                .orderByDesc(Article::getCreatedAt);

        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(Article::getStatus, query.getStatus());
        }
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(Article::getTitle, query.getKeyword());
        }

        IPage<Article> result = baseMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::attachTagsAndAuthor);
        return result;
    }

    @Override
    public Article getMyDetail(Long id, String username) {
        Article article = requireOwnedArticle(id, username);
        attachTagsAndAuthor(article);
        return article;
    }

    @Override
    @Transactional
    public Article createMyArticle(ArticleRequest request, String username) {
        Article article = new Article();
        applyArticleFields(article, request);
        article.setStatus(normalizeAuthorStatus(request.getStatus()));
        article.setReviewReason(null);
        article.setViewCount(0);
        article.setCreatedBy(username);
        save(article);
        replaceArticleTags(article.getId(), request);
        attachTagsAndAuthor(article);
        return article;
    }

    @Override
    @Transactional
    public Article updateMyArticle(Long id, ArticleRequest request, String username) {
        Article article = requireOwnedArticle(id, username);
        if (ArticleStatus.PENDING.equals(article.getStatus())) {
            throw new BusinessException("待审核文章请先撤回再编辑");
        }

        applyArticleFields(article, request);
        if (ArticleStatus.PUBLISHED.equals(article.getStatus())) {
            article.setStatus(ArticleStatus.PENDING);
        } else {
            article.setStatus(normalizeAuthorStatus(request.getStatus()));
        }
        article.setReviewReason(null);
        updateById(article);
        replaceArticleTags(id, request);
        attachTagsAndAuthor(article);
        return article;
    }

    @Override
    @Transactional
    public void deleteMyArticle(Long id, String username) {
        requireOwnedArticle(id, username);
        removeById(id);
    }

    @Override
    @Transactional
    public Article submitMyArticle(Long id, String username) {
        Article article = requireOwnedArticle(id, username);
        if (!ArticleStatus.DRAFT.equals(article.getStatus()) && !ArticleStatus.REJECTED.equals(article.getStatus())) {
            throw new BusinessException("只有草稿或已驳回文章可以提交审核");
        }
        article.setStatus(ArticleStatus.PENDING);
        article.setReviewReason(null);
        updateById(article);
        attachTagsAndAuthor(article);
        return article;
    }

    @Override
    @Transactional
    public Article withdrawMyArticle(Long id, String username) {
        Article article = requireOwnedArticle(id, username);
        if (!ArticleStatus.PENDING.equals(article.getStatus())) {
            throw new BusinessException("只有待审核文章可以撤回");
        }
        article.setStatus(ArticleStatus.DRAFT);
        updateById(article);
        attachTagsAndAuthor(article);
        return article;
    }

    @Override
    public Article getAdminDetail(Long id) {
        Article article = requireArticle(id);
        attachTagsAndAuthor(article);
        return article;
    }

    @Override
    @Transactional
    public Article createAdminArticle(ArticleRequest request) {
        Article article = new Article();
        applyArticleFields(article, request);
        article.setStatus(normalizeAdminStatus(request.getStatus()));
        if (!ArticleStatus.REJECTED.equals(article.getStatus())) {
            article.setReviewReason(null);
        }
        article.setViewCount(0);
        save(article);
        replaceArticleTags(article.getId(), request);
        attachTagsAndAuthor(article);
        return article;
    }

    @Override
    @Transactional
    public Article updateAdminArticle(Long id, ArticleRequest request) {
        Article article = requireArticle(id);
        applyArticleFields(article, request);
        article.setStatus(normalizeAdminStatus(request.getStatus()));
        if (!ArticleStatus.REJECTED.equals(article.getStatus())) {
            article.setReviewReason(null);
        }
        updateById(article);
        replaceArticleTags(id, request);
        attachTagsAndAuthor(article);
        return article;
    }

    @Override
    @Transactional
    public void deleteAdminArticle(Long id) {
        requireArticle(id);
        removeById(id);
    }

    @Override
    @Transactional
    public Article approveArticle(Long id) {
        Article article = requireArticle(id);
        if (!ArticleStatus.PENDING.equals(article.getStatus())) {
            throw new BusinessException("只有待审核文章可以通过");
        }
        article.setStatus(ArticleStatus.PUBLISHED);
        article.setReviewReason(null);
        updateById(article);
        attachTagsAndAuthor(article);
        return article;
    }

    @Override
    @Transactional
    public Article rejectArticle(Long id, String reason) {
        Article article = requireArticle(id);
        if (!ArticleStatus.PENDING.equals(article.getStatus())) {
            throw new BusinessException("只有待审核文章可以驳回");
        }
        article.setStatus(ArticleStatus.REJECTED);
        article.setReviewReason(reason);
        updateById(article);
        attachTagsAndAuthor(article);
        return article;
    }

    @Override
    public IPage<Article> getAdminPage(ArticlePageQuery query) {
        Page<Article> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .orderByDesc(Article::getCreatedAt);

        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(Article::getStatus, query.getStatus());
        }
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(Article::getTitle, query.getKeyword());
        }
        if (query.getAuthor() != null && !query.getAuthor().isEmpty()) {
            wrapper.eq(Article::getCreatedBy, query.getAuthor());
        }

        IPage<Article> result = baseMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::attachTagsAndAuthor);
        return result;
    }

    private Article requireArticle(Long id) {
        Article article = getById(id);
        if (article == null || article.getDeleted() != null && article.getDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }
        return article;
    }

    private Article requireOwnedArticle(Long id, String username) {
        Article article = requireArticle(id);
        if (!username.equals(article.getCreatedBy())) {
            throw new BusinessException("无权操作该文章");
        }
        return article;
    }

    private void applyArticleFields(Article article, ArticleRequest request) {
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
    }

    private String normalizeAuthorStatus(String status) {
        if (ArticleStatus.PENDING.equals(status)) {
            return ArticleStatus.PENDING;
        }
        return ArticleStatus.DRAFT;
    }

    private String normalizeAdminStatus(String status) {
        if (status == null || status.isEmpty()) {
            return ArticleStatus.DRAFT;
        }
        if (!ArticleStatus.isValid(status)) {
            throw new BusinessException("文章状态不合法");
        }
        return status;
    }

    private void replaceArticleTags(Long articleId, ArticleRequest request) {
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, articleId));
        if (request.getTagIds() != null) {
            for (Long tagId : request.getTagIds()) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(articleId);
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
    }

    private void attachTagsAndAuthor(Article article) {
        List<ArticleTag> ats = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getArticleId, article.getId()));
        if (ats != null && !ats.isEmpty()) {
            List<Long> tagIds = ats.stream()
                    .map(ArticleTag::getTagId).collect(Collectors.toList());
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            article.setTags(tags);
        }
        if (article.getCreatedBy() != null) {
            User user = userService.getOne(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getUsername, article.getCreatedBy()));
            if (user != null) {
                article.setAuthorName(user.getNickname());
            }
        }
    }
}
