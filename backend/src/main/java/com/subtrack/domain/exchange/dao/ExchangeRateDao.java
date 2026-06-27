package com.subtrack.domain.exchange.dao;

import com.subtrack.domain.exchange.vo.ExchangeRate;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExchangeRateDao {

    ExchangeRate findFetchedToday(
            @Param("currencyCode") String currencyCode,
            @Param("targetCurrency") String targetCurrency,
            @Param("provider") String provider,
            @Param("fetchedStart") LocalDateTime fetchedStart,
            @Param("fetchedEnd") LocalDateTime fetchedEnd
    );

    ExchangeRate findLatestCached(
            @Param("currencyCode") String currencyCode,
            @Param("targetCurrency") String targetCurrency
    );

    int upsertExchangeRate(ExchangeRate exchangeRate);
}
