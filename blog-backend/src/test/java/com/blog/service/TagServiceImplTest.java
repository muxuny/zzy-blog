package com.blog.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.BusinessException;
import com.blog.entity.Tag;
import com.blog.mapper.TagMapper;
import com.blog.service.impl.TagServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TagServiceImplTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void getAdminPageUsesPagedNewestFirstQuery() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                Tag.class);
        TagMapper tagMapper = mock(TagMapper.class);
        TagServiceImpl service = new TagServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", tagMapper);
        Tag tag = new Tag();
        tag.setId(3L);
        tag.setName("Vue");
        Page<Tag> returned = new Page<>(2, 5);
        returned.setTotal(12);
        returned.setRecords(Collections.singletonList(tag));
        when(tagMapper.selectPage(any(IPage.class), any(Wrapper.class))).thenReturn(returned);

        IPage<Tag> result = service.getAdminPage(2, 5);

        assertThat(result.getTotal()).isEqualTo(12);
        assertThat(result.getRecords()).extracting(Tag::getName).containsExactly("Vue");
        ArgumentCaptor<IPage> pageCaptor = ArgumentCaptor.forClass(IPage.class);
        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(tagMapper).selectPage(pageCaptor.capture(), wrapperCaptor.capture());
        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(2);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(5);
        assertThat(wrapperCaptor.getValue().getSqlSegment())
                .contains("ORDER BY")
                .contains("created_at DESC")
                .contains("id DESC");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void getAdminPageRejectsInvalidPagingBeforeQuerying() {
        TagMapper tagMapper = mock(TagMapper.class);
        TagServiceImpl service = new TagServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", tagMapper);

        assertThatThrownBy(() -> service.getAdminPage(1, 101))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分页参数不合法");

        verify(tagMapper, never()).selectPage(any(IPage.class), any(Wrapper.class));
    }
}
