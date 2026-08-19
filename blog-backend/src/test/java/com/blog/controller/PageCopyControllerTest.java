package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.PageCopyItem;
import com.blog.service.PageCopyService;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PageCopyControllerTest {

    private final PageCopyService pageCopyService = mock(PageCopyService.class);

    private final PageCopyController controller = new PageCopyController(pageCopyService);

    @Test
    void listReturnsPublicCopies() {
        PageCopyItem item = new PageCopyItem();
        item.setCopyKey("home.hero");
        item.setTitle("首页");
        when(pageCopyService.listAll()).thenReturn(Collections.singletonList(item));

        Result<List<PageCopyItem>> result = controller.list();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).extracting(PageCopyItem::getCopyKey).containsExactly("home.hero");
        verify(pageCopyService).listAll();
    }
}
