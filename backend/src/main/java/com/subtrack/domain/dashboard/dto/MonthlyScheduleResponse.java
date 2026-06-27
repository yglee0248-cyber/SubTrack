package com.subtrack.domain.dashboard.dto;

import com.subtrack.domain.dashboard.vo.DashboardSubscription;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MonthlyScheduleResponse {

    private final String yearMonth;
    private final List<Item> items;

    public MonthlyScheduleResponse(String yearMonth, List<Item> items) {
        this.yearMonth = yearMonth;
        this.items = items;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public List<Item> getItems() {
        return items;
    }

    public static class Item {

        private final Long subscriptionId;
        private final String name;
        private final Long categoryId;
        private final String categoryName;
        private final String colorCode;
        private final String icon;
        private final LocalDate paymentDate;
        private final BigDecimal price;
        private final String currency;
        private final BigDecimal rateToKrw;
        private final BigDecimal convertedPriceKrw;
        private final LocalDate exchangeRateDate;
        private final String billingCycle;
        private final String paymentMethod;

        public Item(DashboardSubscription subscription, LocalDate paymentDate) {
            this.subscriptionId = subscription.getSubscriptionId();
            this.name = subscription.getName();
            this.categoryId = subscription.getCategoryId();
            this.categoryName = subscription.getCategoryName();
            this.colorCode = subscription.getColorCode();
            this.icon = subscription.getIcon();
            this.paymentDate = paymentDate;
            this.price = subscription.getPrice();
            this.currency = subscription.getCurrency();
            this.rateToKrw = subscription.getRateToKrw();
            this.convertedPriceKrw = subscription.getConvertedPriceKrw();
            this.exchangeRateDate = subscription.getExchangeRateDate();
            this.billingCycle = subscription.getBillingCycle();
            this.paymentMethod = subscription.getPaymentMethod();
        }

        public Long getSubscriptionId() {
            return subscriptionId;
        }

        public String getName() {
            return name;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public String getColorCode() {
            return colorCode;
        }

        public String getIcon() {
            return icon;
        }

        public LocalDate getPaymentDate() {
            return paymentDate;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public String getCurrency() {
            return currency;
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

        public String getBillingCycle() {
            return billingCycle;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }
    }
}
