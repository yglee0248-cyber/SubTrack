package com.subtrack.domain.dashboard.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DashboardSubscription {

    private Long subscriptionId;
    private String name;
    private Long categoryId;
    private String categoryName;
    private String colorCode;
    private String icon;
    private BigDecimal price;
    private String currency;
    private BigDecimal rateToKrw;
    private BigDecimal convertedPriceKrw;
    private LocalDate exchangeRateDate;
    private String billingCycle;
    private Integer billingAnchorDay;
    private LocalDate billingStartDate;
    private String paymentMethod;
    private LocalDateTime deletedAt;

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

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
