package com.subtrack.domain.dashboard.dto;

import com.subtrack.domain.dashboard.vo.DashboardSummary;
import java.math.BigDecimal;

public class DashboardSummaryResponse {

    private final String yearMonth;
    private final int activeSubscriptionCount;
    private final BigDecimal monthlyExpectedAmount;
    private final int upcomingCount;
    private final int overdueCount;

    public DashboardSummaryResponse(
            String yearMonth,
            int activeSubscriptionCount,
            BigDecimal monthlyExpectedAmount,
            int upcomingCount,
            int overdueCount
    ) {
        this.yearMonth = yearMonth;
        this.activeSubscriptionCount = activeSubscriptionCount;
        this.monthlyExpectedAmount = monthlyExpectedAmount;
        this.upcomingCount = upcomingCount;
        this.overdueCount = overdueCount;
    }

    public static DashboardSummaryResponse of(String yearMonth, DashboardSummary summary) {
        return new DashboardSummaryResponse(
                yearMonth,
                valueOrZero(summary.getActiveSubscriptionCount()),
                amountOrZero(summary.getMonthlyExpectedAmount()),
                valueOrZero(summary.getUpcomingCount()),
                valueOrZero(summary.getOverdueCount())
        );
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public int getActiveSubscriptionCount() {
        return activeSubscriptionCount;
    }

    public BigDecimal getMonthlyExpectedAmount() {
        return monthlyExpectedAmount;
    }

    public int getUpcomingCount() {
        return upcomingCount;
    }

    public int getOverdueCount() {
        return overdueCount;
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static BigDecimal amountOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
