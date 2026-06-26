package com.subtrack.domain.subscription.dto;

import com.subtrack.domain.subscription.vo.Subscription;
import com.subtrack.global.util.PaymentStatusCalculator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SubscriptionListResponse {

    private final List<Item> content;
    private final int page;
    private final int size;
    private final int totalCount;

    public SubscriptionListResponse(List<Item> content, int page, int size, int totalCount) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalCount = totalCount;
    }

    public static SubscriptionListResponse of(List<Subscription> subscriptions, int page, int size, int totalCount) {
        List<Item> content = subscriptions.stream()
                .map(Item::from)
                .toList();

        return new SubscriptionListResponse(content, page, size, totalCount);
    }

    public List<Item> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public static class Item {

        private final Long subscriptionId;
        private final String name;
        private final Long categoryId;
        private final String categoryName;
        private final BigDecimal price;
        private final String currency;
        private final String billingCycle;
        private final LocalDate billingStartDate;
        private final Integer billingAnchorDay;
        private final LocalDate nextPaymentDate;
        private final String paymentMethod;
        private final String status;
        private final String paymentStatus;

        public Item(
                Long subscriptionId,
                String name,
                Long categoryId,
                String categoryName,
                BigDecimal price,
                String currency,
                String billingCycle,
                LocalDate billingStartDate,
                Integer billingAnchorDay,
                LocalDate nextPaymentDate,
                String paymentMethod,
                String status,
                String paymentStatus
        ) {
            this.subscriptionId = subscriptionId;
            this.name = name;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.price = price;
            this.currency = currency;
            this.billingCycle = billingCycle;
            this.billingStartDate = billingStartDate;
            this.billingAnchorDay = billingAnchorDay;
            this.nextPaymentDate = nextPaymentDate;
            this.paymentMethod = paymentMethod;
            this.status = status;
            this.paymentStatus = paymentStatus;
        }

        public static Item from(Subscription subscription) {
            return new Item(
                    subscription.getSubscriptionId(),
                    subscription.getName(),
                    subscription.getCategoryId(),
                    subscription.getCategoryName(),
                    subscription.getPrice(),
                    subscription.getCurrency(),
                    subscription.getBillingCycle(),
                    subscription.getBillingStartDate(),
                    subscription.getBillingAnchorDay(),
                    subscription.getNextPaymentDate(),
                    subscription.getPaymentMethod(),
                    subscription.getStatus(),
                    PaymentStatusCalculator.calculate(subscription.getNextPaymentDate())
            );
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

        public BigDecimal getPrice() {
            return price;
        }

        public String getCurrency() {
            return currency;
        }

        public String getBillingCycle() {
            return billingCycle;
        }

        public LocalDate getBillingStartDate() {
            return billingStartDate;
        }

        public Integer getBillingAnchorDay() {
            return billingAnchorDay;
        }

        public LocalDate getNextPaymentDate() {
            return nextPaymentDate;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public String getStatus() {
            return status;
        }

        public String getPaymentStatus() {
            return paymentStatus;
        }
    }
}
