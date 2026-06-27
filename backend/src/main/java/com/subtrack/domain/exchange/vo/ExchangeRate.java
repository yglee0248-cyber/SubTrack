package com.subtrack.domain.exchange.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExchangeRate {

    private Long exchangeRateId;
    private String currencyCode;
    private String targetCurrency;
    private BigDecimal rateToKrw;
    private LocalDate rateDate;
    private String provider;
    private LocalDateTime fetchedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getExchangeRateId() {
        return exchangeRateId;
    }

    public void setExchangeRateId(Long exchangeRateId) {
        this.exchangeRateId = exchangeRateId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public BigDecimal getRateToKrw() {
        return rateToKrw;
    }

    public void setRateToKrw(BigDecimal rateToKrw) {
        this.rateToKrw = rateToKrw;
    }

    public LocalDate getRateDate() {
        return rateDate;
    }

    public void setRateDate(LocalDate rateDate) {
        this.rateDate = rateDate;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(LocalDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
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
}
