package com.subtrack.domain.subscription.dto;

public class SubscriptionCreateResponse {

    private final Long subscriptionId;

    public SubscriptionCreateResponse(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }
}
