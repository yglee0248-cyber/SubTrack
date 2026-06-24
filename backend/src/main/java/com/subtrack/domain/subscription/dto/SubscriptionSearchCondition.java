package com.subtrack.domain.subscription.dto;

public class SubscriptionSearchCondition {

    private final Long memberId;
    private final String keyword;
    private final Long categoryId;
    private final String status;
    private final String paymentStatus;
    private final int page;
    private final int size;
    private final int offset;

    public SubscriptionSearchCondition(
            Long memberId,
            String keyword,
            Long categoryId,
            String status,
            String paymentStatus,
            int page,
            int size
    ) {
        this.memberId = memberId;
        this.keyword = keyword;
        this.categoryId = categoryId;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.page = page;
        this.size = size;
        this.offset = page * size;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getKeyword() {
        return keyword;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getOffset() {
        return offset;
    }
}
