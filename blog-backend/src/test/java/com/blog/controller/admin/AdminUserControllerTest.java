package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.blog.common.BusinessException;
import com.blog.common.PageResult;
import com.blog.entity.User;
import com.blog.service.UserService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminUserControllerTest {

    private final UserService userService = mock(UserService.class);

    private final AdminUserController controller = new AdminUserController(userService);

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void listReturnsPagedUsersAndClearsPasswords() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("secret");
        Page<User> page = new Page<>(2, 5);
        page.setRecords(Collections.singletonList(user));
        page.setTotal(12);
        when(userService.page(any(Page.class), any(Wrapper.class))).thenReturn(page);

        PageResult<User> result = controller.list(2, 5, null);

        assertThat(result.getTotal()).isEqualTo(12);
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getPassword()).isNull();
    }

    @Test
    void listKeepsRequestParamDefaultsAndOptionalStatus() throws Exception {
        Method list = AdminUserController.class.getMethod("list", long.class, long.class, String.class);

        RequestParam pageParam = list.getParameters()[0].getAnnotation(RequestParam.class);
        RequestParam sizeParam = list.getParameters()[1].getAnnotation(RequestParam.class);
        RequestParam statusParam = list.getParameters()[2].getAnnotation(RequestParam.class);

        assertThat(pageParam.defaultValue()).isEqualTo("1");
        assertThat(sizeParam.defaultValue()).isEqualTo("10");
        assertThat(statusParam.required()).isFalse();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void listFiltersUsersByValidStatus() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                User.class);
        Page<User> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);
        when(userService.page(any(Page.class), any(Wrapper.class))).thenReturn(page);

        PageResult<User> result = controller.list(1, 10, "pending");

        assertThat(result.getTotal()).isZero();
        ArgumentCaptor<Wrapper<User>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(userService).page(any(Page.class), wrapperCaptor.capture());
        assertThat(wrapperCaptor.getValue().getSqlSegment())
                .contains("status =")
                .contains("ORDER BY")
                .contains("created_at DESC");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void listTreatsBlankStatusAsAllUsers() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                User.class);
        Page<User> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);
        when(userService.page(any(Page.class), any(Wrapper.class))).thenReturn(page);

        controller.list(1, 10, " ");

        ArgumentCaptor<Wrapper<User>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(userService).page(any(Page.class), wrapperCaptor.capture());
        assertThat(wrapperCaptor.getValue().getSqlSegment())
                .doesNotContain("status =")
                .contains("ORDER BY")
                .contains("created_at DESC");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void listRejectsInvalidStatusBeforeQuerying() {
        assertThatThrownBy(() -> controller.list(1, 10, "locked"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户状态不合法");

        verify(userService, never()).page(any(Page.class), any(Wrapper.class));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void listRejectsOversizedPageBeforeQuerying() {
        assertThatThrownBy(() -> controller.list(1, 101, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分页参数不合法");

        verify(userService, never()).page(any(Page.class), any(Wrapper.class));
    }
}
