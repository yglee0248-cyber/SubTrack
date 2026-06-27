package com.subtrack.domain.dashboard.dto;

import com.subtrack.domain.dashboard.vo.UpcomingSubscription;
import com.subtrack.global.util.PaymentStatusCalculator;
import java.math.BigDecimal;
import java.time.LocalDate;

public class UpcomingSubscriptionResponse {

    private final Long subscriptionId;
    private final String name;
    private final Long categoryId;
    private final String categoryName;
    private final BigDecimal price;
    private final String currency;
    private final BigDecimal rateToKrw;
    private final BigDecimal convertedPriceKrw;
    private final LocalDate exchangeRateDate;
    private final LocalDate nextPaymentDate;
    private final String paymentStatus;

    public UpcomingSubscriptionResponse(
            Long subscriptionId,
            String name,
            Long categoryId,
            String categoryName,
            BigDecimal price,
            String currency,
            BigDecimal rateToKrw,
            BigDecimal convertedPriceKrw,
            LocalDate exchangeRateDate,
            LocalDate nextPaymentDate,
            String paymentStatus
    ) {
        this.subscriptionId = subscriptionId;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.price = price;
        this.currency = currency;
        this.rateToKrw = rateToKrw;
        this.convertedPriceKrw = convertedPriceKrw;
        this.exchangeRateDate = exchangeRateDate;
        this.nextPaymentDate = nextPaymentDate;
        this.paymentStatus = paymentStatus;
    }

    public static UpcomingSubscriptionResponse from(UpcomingSubscription subscription) {
        return new UpcomingSubscriptionResponse(
                subscription.getSubscriptionId(),
                subscription.getName(),
                subscription.getCategoryId(),
                subscription.getCategoryName(),
                subscription.getPrice(),
                subscription.getCurrency(),
                subscription.getRateToKrw(),
                subscription.getConvertedPriceKrw(),
                subscription.getExchangeRateDate(),
                subscription.getNextPaymentDate(),
                PaymentStatusCalculator.calculate(subscription.getNextPaymentDate())
        );
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public String getName() {
        return name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
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

    public LocalDate getNextPaymentDate() {
        return nextPaymentDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}
