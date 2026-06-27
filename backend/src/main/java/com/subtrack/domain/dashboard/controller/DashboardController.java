package com.subtrack.domain.dashboard.controller;

import com.subtrack.domain.dashboard.dto.CategoryExpenseResponse;
import com.subtrack.domain.dashboard.dto.DashboardSummaryResponse;
import com.subtrack.domain.dashboard.dto.MonthlyExpenseTrendResponse;
import com.subtrack.domain.dashboard.dto.MonthlyScheduleResponse;
import com.subtrack.domain.dashboard.dto.UpcomingSubscriptionResponse;
import com.subtrack.domain.dashboard.service.DashboardService;
import com.subtrack.global.exception.BusinessException;
import com.subtrack.global.exception.ErrorCode;
import com.subtrack.global.response.ApiResponse;
import com.subtrack.global.security.CustomUserDetails;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getSummary(
            @RequestParam(required = false) String yearMonth
    ) {
        return ApiResponse.success(dashboardService.getSummary(getCurrentMemberId(), yearMonth));
    }

    @GetMapping("/upcoming")
    public ApiResponse<List<UpcomingSubscriptionResponse>> getUpcomingSubscriptions(
            @RequestParam(required = false) Integer days
    ) {
        return ApiResponse.success(dashboardService.getUpcomingSubscriptions(getCurrentMemberId(), days));
    }

    @GetMapping("/category-expenses")
    public ApiResponse<List<CategoryExpenseResponse>> getCategoryExpenses(
            @RequestParam(required = false) String yearMonth
    ) {
        return ApiResponse.success(dashboardService.getCategoryExpenses(getCurrentMemberId(), yearMonth));
    }

    @GetMapping("/monthly-expenses")
    public ApiResponse<MonthlyExpenseTrendResponse> getMonthlyExpenses(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return ApiResponse.success(dashboardService.getMonthlyExpenses(getCurrentMemberId(), from, to));
    }

    @GetMapping("/monthly-schedule")
    public ApiResponse<MonthlyScheduleResponse> getMonthlySchedule(
            @RequestParam(required = false) String yearMonth
    ) {
        return ApiResponse.success(dashboardService.getMonthlySchedule(getCurrentMemberId(), yearMonth));
    }

    private Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return userDetails.getMemberId();
    }
}
