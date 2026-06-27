package com.subtrack.domain.dashboard.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpcomingSubscription {

    private Long subscriptionId;
    private String name;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private String currency;
    private BigDecimal rateToKrw;
    private BigDecimal convertedPriceKrw;
    private LocalDate exchangeRateDate;
    private LocalDate nextPaymentDate;

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getRateToKrw() {
        return rateToKrw;
    }

    public void setRateToKrw(BigDecimal rateToKrw) {
        this.rateToKrw = rateToKrw;
    }

    public BigDecimal getConvertedPriceKrw() {
        return convertedPriceKrw;
    }

    public void setConvertedPriceKrw(BigDecimal convertedPriceKrw) {
        this.convertedPriceKrw = convertedPriceKrw;
    }

    public LocalDate getExchangeRateDate() {
        return exchangeRateDate;
    }

    public void setExchangeRateDate(LocalDate exchangeRateDate) {
        this.exchangeRateDate = exchangeRateDate;
    }

    public LocalDate getNextPaymentDate() {
        return nextPaymentDate;
    }

    public void setNextPaymentDate(LocalDate nextPaymentDate) {
        this.nextPaymentDate = nextPaymentDate;
    }
}
