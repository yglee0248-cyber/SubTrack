package com.subtrack.domain.exchange.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subtrack.domain.exchange.dto.ExchangeRateQuote;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FrankfurterExchangeRateClient {

    public static final String PROVIDER = "FRANKFURTER";

    private static final String TARGET_CURRENCY = "KRW";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String baseUrl;

    public FrankfurterExchangeRateClient(
            ObjectMapper objectMapper,
            @Value("${exchange-rate.base-url:https://api.frankfurter.dev}") String baseUrl
    ) {
        this.objectMapper = objectMapper;
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public ExchangeRateQuote fetchRateToKrw(String currencyCode) {
        String normalizedCurrency = normalizeCurrency(currencyCode);
        URI uri = URI.create("%s/v2/rate/%s/%s".formatted(
                baseUrl,
                encode(normalizedCurrency),
                TARGET_CURRENCY
        ));

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Frankfurter API returned status " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            if (!root.hasNonNull("date") || !root.hasNonNull("rate")) {
                throw new IllegalStateException("Frankfurter API response does not contain date or rate");
            }

            LocalDate rateDate = LocalDate.parse(root.path("date").asText());
            BigDecimal rate = root.path("rate").decimalValue();

            return new ExchangeRateQuote(
                    normalizedCurrency,
                    TARGET_CURRENCY,
                    rate,
                    rateDate,
                    PROVIDER,
                    LocalDateTime.now()
            );
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to parse Frankfurter API response", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Frankfurter API request was interrupted", ex);
        }
    }

    private String normalizeCurrency(String currencyCode) {
        if (!StringUtils.hasText(currencyCode)) {
            throw new IllegalArgumentException("currencyCode is required");
        }

        return currencyCode.trim().toUpperCase();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "https://api.frankfurter.dev";
        }

        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
