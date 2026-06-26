package com.subtrack.domain.subscription.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SubscriptionStatusHistory {

    private Long statusHistoryId;
    private Long subscriptionId;
    private String status;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
    private LocalDateTime createdAt;

    public Long getStatusHistoryId() {
        return statusHistoryId;
    }

    public void setStatusHistoryId(Long statusHistoryId) {
        this.statusHistoryId = statusHistoryId;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getEffectiveStartDate() {
        return effectiveStartDate;
    }

    public void setEffectiveStartDate(LocalDate effectiveStartDate) {
        this.effectiveStartDate = effectiveStartDate;
    }

    public LocalDate getEffectiveEndDate() {
        return effectiveEndDate;
    }

    public void setEffectiveEndDate(LocalDate effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
