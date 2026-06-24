package com.subtrack.domain.dashboard.vo;

import java.math.BigDecimal;

public class DashboardSummary {

    private Integer activeSubscriptionCount;
    private BigDecimal monthlyExpectedAmount;
    private Integer upcomingCount;
    private Integer overdueCount;

    public Integer getActiveSubscriptionCount() {
        return activeSubscriptionCount;
    }

    public void setActiveSubscriptionCount(Integer activeSubscriptionCount) {
        this.activeSubscriptionCount = activeSubscriptionCount;
    }

    public BigDecimal getMonthlyExpectedAmount() {
        return monthlyExpectedAmount;
    }

    public void setMonthlyExpectedAmount(BigDecimal monthlyExpectedAmount) {
        this.monthlyExpectedAmount = monthlyExpectedAmount;
    }

    public Integer getUpcomingCount() {
        return upcomingCount;
    }

    public void setUpcomingCount(Integer upcomingCount) {
        this.upcomingCount = upcomingCount;
    }

    public Integer getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(Integer overdueCount) {
        this.overdueCount = overdueCount;
    }
}
