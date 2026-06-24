package com.subtrack.domain.subscription.dto;

public class SubscriptionUpdateResponse {

    private final Long subscriptionId;

    public SubscriptionUpdateResponse(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }
}
