package com.blog.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.blog.common.BusinessException;
import com.blog.dto.PageCopyItem;
import com.blog.dto.PageCopyUpdateRequest;
import com.blog.entity.PageCopy;
import com.blog.mapper.PageCopyMapper;
import com.blog.service.impl.PageCopyServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PageCopyServiceImplTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void listAllEnsuresAndReturnsSeventeenDefaultCopies() {
        initTableInfo();
        PageCopyMapper mapper = mock(PageCopyMapper.class);
        PageCopyServiceImpl service = newService(mapper);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(Collections.emptyList());

        List<PageCopyItem> result = service.listAll();

        assertThat(result).hasSize(17);
        assertThat(result).extracting(PageCopyItem::getCopyKey)
                .contains("home.hero", "tag.index", "admin.profile");
        assertThat(result.get(0).getCopyKey()).isEqualTo("home.hero");
        assertThat(result.get(0).getTitle()).isNotBlank();
        ArgumentCaptor<PageCopy> captor = ArgumentCaptor.forClass(PageCopy.class);
        verify(mapper, times(17)).insert(captor.capture());
        assertThat(captor.getAllValues()).extracting(PageCopy::getCopyKey)
                .containsExactlyElementsOf(result.stream().map(PageCopyItem::getCopyKey).collect(java.util.stream.Collectors.toList()));
    }

    @Test
    void updateRejectsUnknownCopyKey() {
        PageCopyMapper mapper = mock(PageCopyMapper.class);
        PageCopyServiceImpl service = newService(mapper);
        PageCopyUpdateRequest request = new PageCopyUpdateRequest();
        request.setTitle("未知页面");

        assertThatThrownBy(() -> service.updateCopy("unknown.key", request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("页面文案配置不存在");

        verify(mapper, never()).selectOne(any());
        verify(mapper, never()).insert(any());
        verify(mapper, never()).updateById(any());
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void listAllIgnoresConcurrentDefaultInsertConflictAndReloadsTheCopy() {
        initTableInfo();
        PageCopyMapper mapper = mock(PageCopyMapper.class);
        PageCopyServiceImpl service = newService(mapper);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(Collections.emptyList());
        PageCopy concurrentHome = new PageCopy();
        concurrentHome.setId(88L);
        concurrentHome.setCopyKey("home.hero");
        concurrentHome.setEyebrow("并发眉标");
        concurrentHome.setTitle("并发写入标题");
        concurrentHome.setDescription("并发写入描述");
        doAnswer(invocation -> {
            PageCopy pageCopy = invocation.getArgument(0);
            if ("home.hero".equals(pageCopy.getCopyKey())) {
                throw new DuplicateKeyException("duplicate page copy");
            }
            return 1;
        }).when(mapper).insert(any(PageCopy.class));
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(concurrentHome);

        List<PageCopyItem> result = service.listAll();

        assertThat(result).hasSize(17);
        PageCopyItem home = result.stream()
                .filter(item -> "home.hero".equals(item.getCopyKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected home.hero"));
        assertThat(home.getTitle()).isEqualTo("并发写入标题");
        assertThat(home.getPageName()).isEqualTo("首页");
        assertThat(home.getPageGroup()).isEqualTo("公开与用户页");
        verify(mapper, times(17)).insert(any(PageCopy.class));
        verify(mapper, atLeastOnce()).selectOne(any(Wrapper.class));
    }

    @Test
    void writeMethodsAreTransactional() throws Exception {
        assertThat(AnnotatedElementUtils.findMergedAnnotation(
                PageCopyServiceImpl.class.getMethod("listAll"), Transactional.class)).isNotNull();
        assertThat(AnnotatedElementUtils.findMergedAnnotation(
                PageCopyServiceImpl.class.getMethod("updateCopy", String.class, PageCopyUpdateRequest.class),
                Transactional.class)).isNotNull();
        assertThat(AnnotatedElementUtils.findMergedAnnotation(
                PageCopyServiceImpl.class.getMethod("resetAll"), Transactional.class)).isNotNull();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void updateTrimsPageCopyFields() {
        initTableInfo();
        PageCopyMapper mapper = mock(PageCopyMapper.class);
        PageCopyServiceImpl service = newService(mapper);
        PageCopy existing = new PageCopy();
        existing.setId(9L);
        existing.setCopyKey("home.hero");
        existing.setPageName("首页");
        existing.setPageGroup("公开与用户页");
        existing.setSortOrder(10);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(existing);
        PageCopyUpdateRequest request = new PageCopyUpdateRequest();
        request.setEyebrow("  新眉标  ");
        request.setTitle("  新标题  ");
        request.setDescription("  新描述  ");

        PageCopyItem result = service.updateCopy("home.hero", request);

        assertThat(result.getEyebrow()).isEqualTo("新眉标");
        assertThat(result.getTitle()).isEqualTo("新标题");
        assertThat(result.getDescription()).isEqualTo("新描述");
        ArgumentCaptor<PageCopy> captor = ArgumentCaptor.forClass(PageCopy.class);
        verify(mapper).updateById(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(9L);
        assertThat(captor.getValue().getEyebrow()).isEqualTo("新眉标");
        assertThat(captor.getValue().getTitle()).isEqualTo("新标题");
        assertThat(captor.getValue().getDescription()).isEqualTo("新描述");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void updateHandlesConcurrentInsertConflictByUpdatingReloadedCopy() {
        initTableInfo();
        PageCopyMapper mapper = mock(PageCopyMapper.class);
        PageCopyServiceImpl service = newService(mapper);
        PageCopy concurrentHome = new PageCopy();
        concurrentHome.setId(18L);
        concurrentHome.setCopyKey("home.hero");
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(null, concurrentHome);
        doAnswer(invocation -> {
            throw new DuplicateKeyException("duplicate page copy");
        }).when(mapper).insert(any(PageCopy.class));
        PageCopyUpdateRequest request = new PageCopyUpdateRequest();
        request.setEyebrow("新眉标");
        request.setTitle("新标题");
        request.setDescription("新描述");

        PageCopyItem result = service.updateCopy("home.hero", request);

        assertThat(result.getTitle()).isEqualTo("新标题");
        ArgumentCaptor<PageCopy> captor = ArgumentCaptor.forClass(PageCopy.class);
        verify(mapper).updateById(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(18L);
        assertThat(captor.getValue().getTitle()).isEqualTo("新标题");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void resetAllRestoresDefaultCopy() {
        initTableInfo();
        PageCopyMapper mapper = mock(PageCopyMapper.class);
        PageCopyServiceImpl service = newService(mapper);
        PageCopy overridden = new PageCopy();
        overridden.setId(12L);
        overridden.setCopyKey("home.hero");
        overridden.setPageName("首页");
        overridden.setPageGroup("公开与用户页");
        overridden.setEyebrow("自定义");
        overridden.setTitle("自定义标题");
        overridden.setDescription("自定义描述");
        overridden.setSortOrder(10);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(Collections.singletonList(overridden));

        List<PageCopyItem> result = service.resetAll();

        assertThat(result).hasSize(17);
        ArgumentCaptor<PageCopy> updateCaptor = ArgumentCaptor.forClass(PageCopy.class);
        verify(mapper, atLeastOnce()).updateById(updateCaptor.capture());
        PageCopy restoredHome = updateCaptor.getAllValues().stream()
                .filter(item -> "home.hero".equals(item.getCopyKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected home.hero to be restored"));
        assertThat(restoredHome.getEyebrow()).isEqualTo("个人写作库");
        assertThat(restoredHome.getTitle()).isEqualTo("把项目经验写成可以回看的路标。");
        verify(mapper, times(16)).insert(any(PageCopy.class));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void resetAllHandlesConcurrentInsertConflictAndRestoresReloadedCopy() {
        initTableInfo();
        PageCopyMapper mapper = mock(PageCopyMapper.class);
        PageCopyServiceImpl service = newService(mapper);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(Collections.emptyList());
        PageCopy concurrentHome = new PageCopy();
        concurrentHome.setId(28L);
        concurrentHome.setCopyKey("home.hero");
        concurrentHome.setEyebrow("并发自定义");
        concurrentHome.setTitle("并发自定义标题");
        concurrentHome.setDescription("并发自定义描述");
        doAnswer(invocation -> {
            PageCopy pageCopy = invocation.getArgument(0);
            if ("home.hero".equals(pageCopy.getCopyKey())) {
                throw new DuplicateKeyException("duplicate page copy");
            }
            return 1;
        }).when(mapper).insert(any(PageCopy.class));
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(concurrentHome);

        List<PageCopyItem> result = service.resetAll();

        PageCopyItem home = result.stream()
                .filter(item -> "home.hero".equals(item.getCopyKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected home.hero"));
        assertThat(home.getTitle()).isEqualTo("把项目经验写成可以回看的路标。");
        ArgumentCaptor<PageCopy> updateCaptor = ArgumentCaptor.forClass(PageCopy.class);
        verify(mapper).updateById(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getId()).isEqualTo(28L);
        assertThat(updateCaptor.getValue().getTitle()).isEqualTo("把项目经验写成可以回看的路标。");
    }

    private static PageCopyServiceImpl newService(PageCopyMapper mapper) {
        PageCopyServiceImpl service = new PageCopyServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        return service;
    }

    private static void initTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                PageCopy.class);
    }
}
