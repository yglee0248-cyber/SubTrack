package com.subtrack.domain.subscription.dto;

public class SubscriptionDeleteResponse {

    private final Long subscriptionId;
    private final boolean deleted;

    public SubscriptionDeleteResponse(Long subscriptionId, boolean deleted) {
        this.subscriptionId = subscriptionId;
        this.deleted = deleted;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
