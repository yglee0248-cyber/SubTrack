package com.subtrack.domain.exchange.service;

import com.subtrack.domain.exchange.client.FrankfurterExchangeRateClient;
import com.subtrack.domain.exchange.dao.ExchangeRateDao;
import com.subtrack.domain.exchange.dto.ExchangeRateConversion;
import com.subtrack.domain.exchange.dto.ExchangeRateQuote;
import com.subtrack.domain.exchange.vo.ExchangeRate;
import com.subtrack.global.exception.BusinessException;
import com.subtrack.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    private static final String KRW = "KRW";
    private static final String TARGET_CURRENCY = KRW;
    private static final String SYSTEM_PROVIDER = "SYSTEM";
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("KRW", "USD", "JPY", "CNY", "EUR");
    private static final Set<String> INTEGER_CURRENCIES = Set.of("KRW", "JPY");
    private static final BigDecimal KRW_RATE = BigDecimal.ONE.setScale(6, RoundingMode.HALF_UP);

    private final ExchangeRateDao exchangeRateDao;
    private final FrankfurterExchangeRateClient frankfurterClient;

    public ExchangeRateService(
            ExchangeRateDao exchangeRateDao,
            FrankfurterExchangeRateClient frankfurterClient
    ) {
        this.exchangeRateDao = exchangeRateDao;
        this.frankfurterClient = frankfurterClient;
    }

    public boolean isSupportedCurrency(String currencyCode) {
        return SUPPORTED_CURRENCIES.contains(normalizeCurrency(currencyCode));
    }

    public void validateSupportedCurrency(String currencyCode) {
        if (!isSupportedCurrency(currencyCode)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "지원하지 않는 통화입니다.");
        }
    }

    public void validatePriceScale(BigDecimal price, String currencyCode) {
        if (price == null) {
            return;
        }

        String currency = normalizeCurrency(currencyCode);
        int scale = Math.max(0, price.stripTrailingZeros().scale());

        if (INTEGER_CURRENCIES.contains(currency) && scale > 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "KRW와 JPY 금액은 정수로 입력해야 합니다.");
        }

        if (!INTEGER_CURRENCIES.contains(currency) && scale > 2) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "외화 금액은 소수 2자리까지 입력할 수 있습니다.");
        }
    }

    @Transactional
    public ExchangeRateQuote getRateToKrw(String currencyCode) {
        String currency = normalizeCurrency(currencyCode);
        validateSupportedCurrency(currency);

        if (KRW.equals(currency)) {
            return buildKrwQuote();
        }

        LocalDate today = LocalDate.now(SERVICE_ZONE);
        LocalDateTime fetchedStart = today.atStartOfDay();
        LocalDateTime fetchedEnd = today.plusDays(1).atStartOfDay();
        ExchangeRate todayCached = exchangeRateDao.findFetchedToday(
                currency,
                TARGET_CURRENCY,
                FrankfurterExchangeRateClient.PROVIDER,
                fetchedStart,
                fetchedEnd
        );

        if (todayCached != null) {
            return toQuote(todayCached);
        }

        try {
            ExchangeRateQuote fetchedQuote = frankfurterClient.fetchRateToKrw(currency);
            upsertQuote(fetchedQuote);
            return fetchedQuote;
        } catch (RuntimeException ex) {
            log.warn("Failed to fetch exchange rate. currency={}, fallback=latest-cache", currency, ex);
            ExchangeRate latestCached = exchangeRateDao.findLatestCached(currency, TARGET_CURRENCY);
            if (latestCached != null) {
                return toQuote(latestCached);
            }

            throw new BusinessException(ErrorCode.EXCHANGE_RATE_UNAVAILABLE);
        }
    }

    @Transactional
    public Map<String, ExchangeRateQuote> getRatesToKrw(Collection<String> currencyCodes) {
        Map<String, ExchangeRateQuote> result = new LinkedHashMap<>();

        if (currencyCodes == null) {
            return result;
        }

        for (String currencyCode : currencyCodes) {
            String currency = normalizeCurrency(currencyCode);
            if (!result.containsKey(currency)) {
                result.put(currency, getRateToKrw(currency));
            }
        }

        return result;
    }

    @Transactional
    public ExchangeRateConversion convertToKrw(BigDecimal amount, String currencyCode) {
        return convertToKrw(amount, getRateToKrw(currencyCode));
    }

    public ExchangeRateConversion convertToKrw(BigDecimal amount, ExchangeRateQuote quote) {
        BigDecimal sourceAmount = amount == null ? BigDecimal.ZERO : amount;
        BigDecimal convertedPriceKrw = sourceAmount
                .multiply(quote.getRateToKrw())
                .setScale(0, RoundingMode.HALF_UP);
        LocalDate exchangeRateDate = KRW.equals(quote.getCurrencyCode()) ? null : quote.getRateDate();

        return new ExchangeRateConversion(
                quote.getRateToKrw(),
                convertedPriceKrw,
                exchangeRateDate
        );
    }

    private void upsertQuote(ExchangeRateQuote quote) {
        if (KRW.equals(quote.getCurrencyCode())) {
            return;
        }

        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setCurrencyCode(quote.getCurrencyCode());
        exchangeRate.setTargetCurrency(quote.getTargetCurrency());
        exchangeRate.setRateToKrw(quote.getRateToKrw());
        exchangeRate.setRateDate(quote.getRateDate() == null ? LocalDate.now(SERVICE_ZONE) : quote.getRateDate());
        exchangeRate.setProvider(quote.getProvider());
        exchangeRate.setFetchedAt(quote.getFetchedAt() == null ? LocalDateTime.now(SERVICE_ZONE) : quote.getFetchedAt());
        exchangeRateDao.upsertExchangeRate(exchangeRate);
    }

    private ExchangeRateQuote buildKrwQuote() {
        return new ExchangeRateQuote(
                KRW,
                TARGET_CURRENCY,
                KRW_RATE,
                null,
                SYSTEM_PROVIDER,
                LocalDateTime.now(SERVICE_ZONE)
        );
    }

    private ExchangeRateQuote toQuote(ExchangeRate exchangeRate) {
        return new ExchangeRateQuote(
                exchangeRate.getCurrencyCode(),
                exchangeRate.getTargetCurrency(),
                exchangeRate.getRateToKrw(),
                SYSTEM_PROVIDER.equals(exchangeRate.getProvider()) && KRW.equals(exchangeRate.getCurrencyCode())
                        ? null
                        : exchangeRate.getRateDate(),
                exchangeRate.getProvider(),
                exchangeRate.getFetchedAt()
        );
    }

    private String normalizeCurrency(String currencyCode) {
        if (!StringUtils.hasText(currencyCode)) {
            return KRW;
        }

        return currencyCode.trim().toUpperCase(Locale.ROOT);
    }
}
