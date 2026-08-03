package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.BusinessException;
import com.blog.common.PageResult;
import com.blog.entity.User;
import com.blog.service.UserService;
import org.junit.jupiter.api.Test;

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

        PageResult<User> result = controller.list(2, 5);

        assertThat(result.getTotal()).isEqualTo(12);
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getPassword()).isNull();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void listRejectsOversizedPageBeforeQuerying() {
        assertThatThrownBy(() -> controller.list(1, 101))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分页参数不合法");

        verify(userService, never()).page(any(Page.class), any(Wrapper.class));
    }
}
