package com.subtrack.domain.subscription.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Subscription {

    private Long subscriptionId;
    private Long memberId;
    private Long categoryId;
    private String categoryName;
    private String name;
    private BigDecimal price;
    private String currency;
    private BigDecimal rateToKrw;
    private BigDecimal convertedPriceKrw;
    private LocalDate exchangeRateDate;
    private String billingCycle;
    private Integer billingAnchorDay;
    private LocalDate billingStartDate;
    private LocalDate nextPaymentDate;
    private LocalDate statusEffectiveDate;
    private String paymentMethod;
    private String status;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public Integer getBillingAnchorDay() {
        return billingAnchorDay;
    }

    public void setBillingAnchorDay(Integer billingAnchorDay) {
        this.billingAnchorDay = billingAnchorDay;
    }

    public LocalDate getBillingStartDate() {
        return billingStartDate;
    }

    public void setBillingStartDate(LocalDate billingStartDate) {
        this.billingStartDate = billingStartDate;
    }

    public LocalDate getNextPaymentDate() {
        return nextPaymentDate;
    }

    public void setNextPaymentDate(LocalDate nextPaymentDate) {
        this.nextPaymentDate = nextPaymentDate;
    }

    public LocalDate getStatusEffectiveDate() {
        return statusEffectiveDate;
    }

    public void setStatusEffectiveDate(LocalDate statusEffectiveDate) {
        this.statusEffectiveDate = statusEffectiveDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
