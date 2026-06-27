package com.subtrack.domain.exchange.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExchangeRateQuote {

    private final String currencyCode;
    private final String targetCurrency;
    private final BigDecimal rateToKrw;
    private final LocalDate rateDate;
    private final String provider;
    private final LocalDateTime fetchedAt;

    public ExchangeRateQuote(
            String currencyCode,
            String targetCurrency,
            BigDecimal rateToKrw,
            LocalDate rateDate,
            String provider,
            LocalDateTime fetchedAt
    ) {
        this.currencyCode = currencyCode;
        this.targetCurrency = targetCurrency;
        this.rateToKrw = rateToKrw;
        this.rateDate = rateDate;
        this.provider = provider;
        this.fetchedAt = fetchedAt;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public BigDecimal getRateToKrw() {
        return rateToKrw;
    }

    public LocalDate getRateDate() {
        return rateDate;
    }

    public String getProvider() {
        return provider;
    }

    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }
}
