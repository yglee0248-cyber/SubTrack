package com.subtrack.domain.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public class MonthlyExpenseTrendResponse {

    private final String from;
    private final String to;
    private final String currency;
    private final List<Item> items;

    public MonthlyExpenseTrendResponse(String from, String to, List<Item> items) {
        this.from = from;
        this.to = to;
        this.currency = "KRW";
        this.items = items;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getCurrency() {
        return currency;
    }

    public List<Item> getItems() {
        return items;
    }

    public static class Item {

        private final String yearMonth;
        private final BigDecimal expectedAmountKrw;
        private final int subscriptionCount;

        public Item(String yearMonth, BigDecimal expectedAmountKrw, int subscriptionCount) {
            this.yearMonth = yearMonth;
            this.expectedAmountKrw = expectedAmountKrw;
            this.subscriptionCount = subscriptionCount;
        }

        public String getYearMonth() {
            return yearMonth;
        }

        public BigDecimal getExpectedAmountKrw() {
            return expectedAmountKrw;
        }

        public int getSubscriptionCount() {
            return subscriptionCount;
        }
    }
}
