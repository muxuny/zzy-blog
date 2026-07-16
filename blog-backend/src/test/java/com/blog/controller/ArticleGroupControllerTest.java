package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.ArticleGroupRequest;
import com.blog.dto.ArticleGroupSummary;
import com.blog.entity.ArticleGroup;
import com.blog.service.ArticleGroupService;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArticleGroupControllerTest {

    private final ArticleGroupService articleGroupService = mock(ArticleGroupService.class);

    private final ArticleGroupController articleGroupController = new ArticleGroupController(articleGroupService);

    private final Principal principal = () -> "alice";

    @Test
    void listUsesCurrentPrincipal() {
        ArticleGroupSummary summary = new ArticleGroupSummary();
        summary.setId(1L);
        summary.setName("Ideas");
        summary.setArticleCount(2L);
        when(articleGroupService.listMyGroups("alice")).thenReturn(Collections.singletonList(summary));

        Result<List<ArticleGroupSummary>> result = articleGroupController.list(principal);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals(2L, result.getData().get(0).getArticleCount());
        verify(articleGroupService).listMyGroups("alice");
    }

    @Test
    void createUsesCurrentPrincipal() {
        ArticleGroupRequest request = request("Ideas");
        ArticleGroup group = group(1L, "Ideas");
        when(articleGroupService.createMyGroup(request, "alice")).thenReturn(group);

        Result<ArticleGroup> result = articleGroupController.create(request, principal);

        assertEquals(200, result.getCode());
        assertEquals("Ideas", result.getData().getName());
        verify(articleGroupService).createMyGroup(request, "alice");
    }

    @Test
    void updateUsesCurrentPrincipal() {
        ArticleGroupRequest request = request("Later");
        ArticleGroup group = group(1L, "Later");
        when(articleGroupService.updateMyGroup(1L, request, "alice")).thenReturn(group);

        Result<ArticleGroup> result = articleGroupController.update(1L, request, principal);

        assertEquals(200, result.getCode());
        assertEquals("Later", result.getData().getName());
        verify(articleGroupService).updateMyGroup(1L, request, "alice");
    }

    @Test
    void deleteUsesCurrentPrincipal() {
        Result<Void> result = articleGroupController.delete(1L, principal);

        assertEquals(200, result.getCode());
        verify(articleGroupService).deleteMyGroup(1L, "alice");
    }

    private static ArticleGroupRequest request(String name) {
        ArticleGroupRequest request = new ArticleGroupRequest();
        request.setName(name);
        return request;
    }

    private static ArticleGroup group(Long id, String name) {
        ArticleGroup group = new ArticleGroup();
        group.setId(id);
        group.setName(name);
        return group;
    }
}
