package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.entity.Tag;
import com.blog.service.TagService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminTagControllerTest {

    private final TagService tagService = mock(TagService.class);

    private final AdminTagController controller = new AdminTagController(tagService);

    @Test
    void classAllowsOnlyAdmins() {
        PreAuthorize annotation = AdminTagController.class.getAnnotation(PreAuthorize.class);

        assertEquals("hasRole('ADMIN')", annotation.value());
    }

    @Test
    void listReturnsPagedTags() {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Java");
        Page<Tag> page = new Page<>(2, 5);
        page.setTotal(12);
        page.setRecords(Collections.singletonList(tag));
        when(tagService.getAdminPage(2, 5)).thenReturn(page);

        PageResult<Tag> result = controller.list(2, 5);

        assertThat(result.getData()).extracting(Tag::getName).containsExactly("Java");
        assertThat(result.getTotal()).isEqualTo(12);
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(5);
        verify(tagService).getAdminPage(2, 5);
    }

    @Test
    void createDelegatesToService() {
        Tag tag = new Tag();
        tag.setName("Ops");
        when(tagService.createTag("Ops")).thenReturn(tag);

        Result<Tag> result = controller.create(Collections.singletonMap("name", "Ops"));

        assertThat(result.getData().getName()).isEqualTo("Ops");
        verify(tagService).createTag("Ops");
    }
}
