package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.BusinessException;
import com.blog.dto.PageCopyItem;
import com.blog.dto.PageCopyUpdateRequest;
import com.blog.entity.PageCopy;
import com.blog.mapper.PageCopyMapper;
import com.blog.service.PageCopyService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 页面文案配置服务实现，负责默认配置补齐和管理员覆盖。
 */
@Service
public class PageCopyServiceImpl extends ServiceImpl<PageCopyMapper, PageCopy>
        implements PageCopyService {

    private static final List<DefaultCopy> DEFAULT_COPIES = Collections.unmodifiableList(Arrays.asList(
            copy("home.hero", "首页", "公开与用户页", "个人写作库",
                    "把项目经验写成可以回看的路标。",
                    "这里记录开发实践、阅读笔记和阶段性思考。文章不追求热闹，更关心一个问题从出现到解决的过程。", 10),
            copy("tag.index", "标签页", "公开与用户页", "话题索引",
                    "{tagName}",
                    "按标签收束后的文章流，只保留同一话题下的公开记录。", 20),
            copy("auth.login", "登录", "公开与用户页", "登录后继续",
                    "接上刚才的阅读和创作。",
                    "登录后会优先回到你刚才要打开的页面；没有指定入口时，会进入首页，管理员可直接进入后台。", 30),
            copy("auth.register", "注册", "公开与用户页", "新入口",
                    "给自己的内容留一个稳定身份。",
                    "注册后等待审核，通过后就可以继续阅读、写文章和进入对应的管理入口。", 40),
            copy("reading.overview", "我的阅读", "公开与用户页", "Reading desk",
                    "我的阅读更像一个安静的续接台。",
                    "这里不负责发现热门内容，只负责把读者和自己的阅读轨迹接起来。轻微动态集中在继续阅读和历史焦点上。", 50),
            copy("reading.history", "阅读历史", "公开与用户页", "Reading trail",
                    "阅读历史",
                    "按时间回看读过的文章，保留标题快照，也允许清理不再需要的单条记录。", 60),
            copy("favorites.index", "我的收藏", "公开与用户页", "Pinned index",
                    "我的收藏",
                    "把值得回看的文章收进一个轻量索引，筛选仍然服务于快速返回内容本身。", 70),
            copy("creator.articles", "创作文章", "创作中心", "Creator console",
                    "创作空间保留控制台感，但触感更轻。",
                    "创作者最需要效率，所以动态只用于聚焦当前行、状态筛选和预览反馈。它应该让后台工作更顺手，而不是更花。", 110),
            copy("creator.article.create", "创作写文章", "创作中心", "创作中心",
                    "写文章",
                    "把标题、摘要、分组和正文放在同一个工作流里，状态动作保持在明确的底部区域。", 120),
            copy("creator.article.edit", "创作编辑文章", "创作中心", "创作中心",
                    "编辑文章",
                    "把标题、摘要、分组和正文放在同一个工作流里，状态动作保持在明确的底部区域。", 130),
            copy("admin.articles", "后台文章管理", "后台管理", "内容审核",
                    "文章管理",
                    "审核、筛选和维护全站文章，确保公开内容状态清晰。", 220),
            copy("admin.article.create", "后台写文章", "后台管理", "内容创建",
                    "写文章",
                    "以管理员身份创建文章，并直接处理发布状态。", 230),
            copy("admin.article.edit", "后台编辑文章", "后台管理", "内容编辑",
                    "编辑文章",
                    "维护文章正文、标签、可见性和审核状态。", 240),
            copy("admin.resources", "资源管理", "后台管理", "内容资源",
                    "资源管理",
                    "统一维护标签库和图片素材，支撑公开筛选与文章编辑。", 250),
            copy("admin.users", "用户管理", "后台管理", "账号审核",
                    "用户管理",
                    "处理账号审核和禁用状态，控制创作与后台入口权限。", 260),
            copy("admin.profile", "个人资料", "后台管理", "个人资料",
                    "个人资料",
                    "查看当前管理员账号信息和后台访问状态。", 270)
    ));

    private static final Map<String, DefaultCopy> DEFAULT_COPY_MAP = buildDefaultCopyMap();

    @Override
    @Transactional
    public List<PageCopyItem> listAll() {
        Map<String, PageCopy> existingMap = selectExistingMap();
        List<PageCopyItem> result = new ArrayList<>();
        for (DefaultCopy defaultCopy : DEFAULT_COPIES) {
            PageCopy pageCopy = existingMap.get(defaultCopy.copyKey);
            if (pageCopy == null) {
                pageCopy = insertMissingDefault(defaultCopy);
            } else {
                applyStableFields(pageCopy, defaultCopy);
            }
            result.add(PageCopyItem.from(pageCopy));
        }
        return result;
    }

    @Override
    @Transactional
    public PageCopyItem updateCopy(String copyKey, PageCopyUpdateRequest request) {
        DefaultCopy defaultCopy = DEFAULT_COPY_MAP.get(copyKey);
        if (defaultCopy == null) {
            throw new BusinessException("页面文案配置不存在");
        }
        String title = trimToEmpty(request.getTitle());
        if (title.isEmpty()) {
            throw new BusinessException("页面标题不能为空");
        }
        PageCopy pageCopy = baseMapper.selectOne(new LambdaQueryWrapper<PageCopy>()
                .eq(PageCopy::getCopyKey, copyKey));
        if (pageCopy == null) {
            pageCopy = insertOrUpdateConcurrentCopy(defaultCopy, request);
        } else {
            applyStableFields(pageCopy, defaultCopy);
            applyEditableFields(pageCopy, request);
            baseMapper.updateById(pageCopy);
        }
        return PageCopyItem.from(pageCopy);
    }

    @Override
    @Transactional
    public List<PageCopyItem> resetAll() {
        Map<String, PageCopy> existingMap = selectExistingMap();
        List<PageCopyItem> result = new ArrayList<>();
        for (DefaultCopy defaultCopy : DEFAULT_COPIES) {
            PageCopy pageCopy = existingMap.get(defaultCopy.copyKey);
            if (pageCopy == null) {
                pageCopy = insertOrResetConcurrentCopy(defaultCopy);
            } else {
                defaultCopy.applyTo(pageCopy);
                baseMapper.updateById(pageCopy);
            }
            result.add(PageCopyItem.from(pageCopy));
        }
        return result;
    }

    private PageCopy insertMissingDefault(DefaultCopy defaultCopy) {
        PageCopy pageCopy = defaultCopy.toEntity();
        try {
            baseMapper.insert(pageCopy);
            return pageCopy;
        } catch (DuplicateKeyException e) {
            PageCopy concurrent = baseMapper.selectOne(new LambdaQueryWrapper<PageCopy>()
                    .eq(PageCopy::getCopyKey, defaultCopy.copyKey));
            if (concurrent != null) {
                applyStableFields(concurrent, defaultCopy);
                return concurrent;
            }
            return pageCopy;
        }
    }

    private PageCopy insertOrUpdateConcurrentCopy(DefaultCopy defaultCopy, PageCopyUpdateRequest request) {
        PageCopy pageCopy = defaultCopy.toEntity();
        applyEditableFields(pageCopy, request);
        try {
            baseMapper.insert(pageCopy);
            return pageCopy;
        } catch (DuplicateKeyException e) {
            PageCopy concurrent = selectByCopyKey(defaultCopy.copyKey);
            if (concurrent == null) {
                throw e;
            }
            applyStableFields(concurrent, defaultCopy);
            applyEditableFields(concurrent, request);
            baseMapper.updateById(concurrent);
            return concurrent;
        }
    }

    private PageCopy insertOrResetConcurrentCopy(DefaultCopy defaultCopy) {
        PageCopy pageCopy = defaultCopy.toEntity();
        try {
            baseMapper.insert(pageCopy);
            return pageCopy;
        } catch (DuplicateKeyException e) {
            PageCopy concurrent = selectByCopyKey(defaultCopy.copyKey);
            if (concurrent == null) {
                throw e;
            }
            defaultCopy.applyTo(concurrent);
            baseMapper.updateById(concurrent);
            return concurrent;
        }
    }

    private PageCopy selectByCopyKey(String copyKey) {
        return baseMapper.selectOne(new LambdaQueryWrapper<PageCopy>()
                .eq(PageCopy::getCopyKey, copyKey));
    }

    private Map<String, PageCopy> selectExistingMap() {
        List<PageCopy> pageCopies = baseMapper.selectList(new LambdaQueryWrapper<PageCopy>()
                .orderByAsc(PageCopy::getSortOrder)
                .orderByAsc(PageCopy::getId));
        Map<String, PageCopy> existingMap = new LinkedHashMap<>();
        for (PageCopy pageCopy : pageCopies) {
            if (DEFAULT_COPY_MAP.containsKey(pageCopy.getCopyKey())
                    && !existingMap.containsKey(pageCopy.getCopyKey())) {
                existingMap.put(pageCopy.getCopyKey(), pageCopy);
            }
        }
        return existingMap;
    }

    private static void applyStableFields(PageCopy pageCopy, DefaultCopy defaultCopy) {
        pageCopy.setPageName(defaultCopy.pageName);
        pageCopy.setPageGroup(defaultCopy.pageGroup);
        pageCopy.setSortOrder(defaultCopy.sortOrder);
    }

    private static void applyEditableFields(PageCopy pageCopy, PageCopyUpdateRequest request) {
        pageCopy.setEyebrow(trimToEmpty(request.getEyebrow()));
        pageCopy.setTitle(trimToEmpty(request.getTitle()));
        pageCopy.setDescription(trimToEmpty(request.getDescription()));
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static DefaultCopy copy(String copyKey, String pageName, String pageGroup,
                                    String eyebrow, String title, String description, int sortOrder) {
        return new DefaultCopy(copyKey, pageName, pageGroup, eyebrow, title, description, sortOrder);
    }

    private static Map<String, DefaultCopy> buildDefaultCopyMap() {
        Map<String, DefaultCopy> map = new LinkedHashMap<>();
        for (DefaultCopy defaultCopy : DEFAULT_COPIES) {
            map.put(defaultCopy.copyKey, defaultCopy);
        }
        return Collections.unmodifiableMap(map);
    }

    private static final class DefaultCopy {
        private final String copyKey;
        private final String pageName;
        private final String pageGroup;
        private final String eyebrow;
        private final String title;
        private final String description;
        private final Integer sortOrder;

        private DefaultCopy(String copyKey, String pageName, String pageGroup,
                            String eyebrow, String title, String description, Integer sortOrder) {
            this.copyKey = copyKey;
            this.pageName = pageName;
            this.pageGroup = pageGroup;
            this.eyebrow = eyebrow;
            this.title = title;
            this.description = description;
            this.sortOrder = sortOrder;
        }

        private PageCopy toEntity() {
            PageCopy pageCopy = new PageCopy();
            applyTo(pageCopy);
            return pageCopy;
        }

        private void applyTo(PageCopy pageCopy) {
            pageCopy.setCopyKey(copyKey);
            pageCopy.setPageName(pageName);
            pageCopy.setPageGroup(pageGroup);
            pageCopy.setEyebrow(eyebrow);
            pageCopy.setTitle(title);
            pageCopy.setDescription(description);
            pageCopy.setSortOrder(sortOrder);
        }
    }
}
