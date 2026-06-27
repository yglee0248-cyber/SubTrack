package com.subtrack.domain.subscription.dto;

import com.subtrack.domain.subscription.vo.Subscription;
import com.subtrack.global.util.PaymentStatusCalculator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SubscriptionDetailResponse {

    private final Long subscriptionId;
    private final Long categoryId;
    private final String categoryName;
    private final String name;
    private final BigDecimal price;
    private final String currency;
    private final BigDecimal rateToKrw;
    private final BigDecimal convertedPriceKrw;
    private final LocalDate exchangeRateDate;
    private final String billingCycle;
    private final LocalDate billingStartDate;
    private final Integer billingAnchorDay;
    private final LocalDate nextPaymentDate;
    private final LocalDate statusEffectiveDate;
    private final String paymentMethod;
    private final String memo;
    private final String status;
    private final String paymentStatus;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public SubscriptionDetailResponse(
            Long subscriptionId,
            Long categoryId,
            String categoryName,
            String name,
            BigDecimal price,
            String currency,
            BigDecimal rateToKrw,
            BigDecimal convertedPriceKrw,
            LocalDate exchangeRateDate,
            String billingCycle,
            LocalDate billingStartDate,
            Integer billingAnchorDay,
            LocalDate nextPaymentDate,
            LocalDate statusEffectiveDate,
            String paymentMethod,
            String memo,
            String status,
            String paymentStatus,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.subscriptionId = subscriptionId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.rateToKrw = rateToKrw;
        this.convertedPriceKrw = convertedPriceKrw;
        this.exchangeRateDate = exchangeRateDate;
        this.billingCycle = billingCycle;
        this.billingStartDate = billingStartDate;
        this.billingAnchorDay = billingAnchorDay;
        this.nextPaymentDate = nextPaymentDate;
        this.statusEffectiveDate = statusEffectiveDate;
        this.paymentMethod = paymentMethod;
        this.memo = memo;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SubscriptionDetailResponse from(Subscription subscription) {
        return new SubscriptionDetailResponse(
                subscription.getSubscriptionId(),
                subscription.getCategoryId(),
                subscription.getCategoryName(),
                subscription.getName(),
                subscription.getPrice(),
                subscription.getCurrency(),
                subscription.getRateToKrw(),
                subscription.getConvertedPriceKrw(),
                subscription.getExchangeRateDate(),
                subscription.getBillingCycle(),
                subscription.getBillingStartDate(),
                subscription.getBillingAnchorDay(),
                subscription.getNextPaymentDate(),
                subscription.getStatusEffectiveDate(),
                subscription.getPaymentMethod(),
                subscription.getMemo(),
                subscription.getStatus(),
                PaymentStatusCalculator.calculate(subscription.getNextPaymentDate()),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getRateToKrw() {
        return rateToKrw;
    }

    public BigDecimal getConvertedPriceKrw() {
        return convertedPriceKrw;
    }

    public LocalDate getExchangeRateDate() {
        return exchangeRateDate;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public LocalDate getBillingStartDate() {
        return billingStartDate;
    }

    public Integer getBillingAnchorDay() {
        return billingAnchorDay;
    }

    public LocalDate getNextPaymentDate() {
        return nextPaymentDate;
    }

    public LocalDate getStatusEffectiveDate() {
        return statusEffectiveDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getMemo() {
        return memo;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
