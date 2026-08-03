package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.dto.AdminDashboardOverview;
import com.blog.service.AdminDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminDashboardControllerTest {

    private final AdminDashboardService adminDashboardService = mock(AdminDashboardService.class);

    private final AdminDashboardController controller = new AdminDashboardController(adminDashboardService);

    @Test
    void classAllowsOnlyAdmins() {
        PreAuthorize annotation = AdminDashboardController.class.getAnnotation(PreAuthorize.class);

        assertEquals("hasRole('ADMIN')", annotation.value());
    }

    @Test
    void overviewReturnsServicePayload() {
        AdminDashboardOverview overview = new AdminDashboardOverview();
        overview.getMetrics().setTotalArticles(8);
        when(adminDashboardService.getOverview()).thenReturn(overview);

        Result<AdminDashboardOverview> result = controller.overview();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getMetrics().getTotalArticles()).isEqualTo(8);
        verify(adminDashboardService).getOverview();
    }
}
