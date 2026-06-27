package com.subtrack.domain.dashboard.dto;

import com.subtrack.domain.dashboard.vo.DashboardSummary;
import java.math.BigDecimal;

public class DashboardSummaryResponse {

    private final String yearMonth;
    private final int activeSubscriptionCount;
    private final int monthlySubscriptionCount;
    private final BigDecimal monthlyExpectedAmount;
    private final BigDecimal monthlyExpectedAmountKrw;
    private final int upcomingCount;
    private final int dueTodayCount;
    private final int todayPaymentCount;
    private final String currency;

    public DashboardSummaryResponse(
            String yearMonth,
            int activeSubscriptionCount,
            BigDecimal monthlyExpectedAmount,
            int upcomingCount,
            int dueTodayCount
    ) {
        this.yearMonth = yearMonth;
        this.activeSubscriptionCount = activeSubscriptionCount;
        this.monthlySubscriptionCount = activeSubscriptionCount;
        this.monthlyExpectedAmount = monthlyExpectedAmount;
        this.monthlyExpectedAmountKrw = monthlyExpectedAmount;
        this.upcomingCount = upcomingCount;
        this.dueTodayCount = dueTodayCount;
        this.todayPaymentCount = dueTodayCount;
        this.currency = "KRW";
    }

    public static DashboardSummaryResponse of(String yearMonth, DashboardSummary summary) {
        return new DashboardSummaryResponse(
                yearMonth,
                valueOrZero(summary.getActiveSubscriptionCount()),
                amountOrZero(summary.getMonthlyExpectedAmount()),
                valueOrZero(summary.getUpcomingCount()),
                valueOrZero(summary.getDueTodayCount())
        );
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public int getActiveSubscriptionCount() {
        return activeSubscriptionCount;
    }

    public int getMonthlySubscriptionCount() {
        return monthlySubscriptionCount;
    }

    public BigDecimal getMonthlyExpectedAmount() {
        return monthlyExpectedAmount;
    }

    public BigDecimal getMonthlyExpectedAmountKrw() {
        return monthlyExpectedAmountKrw;
    }

    public int getUpcomingCount() {
        return upcomingCount;
    }

    public int getDueTodayCount() {
        return dueTodayCount;
    }

    public int getTodayPaymentCount() {
        return todayPaymentCount;
    }

    public String getCurrency() {
        return currency;
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static BigDecimal amountOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
