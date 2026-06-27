package com.subtrack.domain.exchange.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExchangeRateConversion {

    private final BigDecimal rateToKrw;
    private final BigDecimal convertedPriceKrw;
    private final LocalDate exchangeRateDate;

    public ExchangeRateConversion(
            BigDecimal rateToKrw,
            BigDecimal convertedPriceKrw,
            LocalDate exchangeRateDate
    ) {
        this.rateToKrw = rateToKrw;
        this.convertedPriceKrw = convertedPriceKrw;
        this.exchangeRateDate = exchangeRateDate;
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
}
