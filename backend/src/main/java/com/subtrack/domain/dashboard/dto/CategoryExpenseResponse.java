package com.subtrack.domain.dashboard.dto;

import com.subtrack.domain.dashboard.vo.CategoryExpense;
import java.math.BigDecimal;

public class CategoryExpenseResponse {

    private final Long categoryId;
    private final String categoryName;
    private final String colorCode;
    private final String icon;
    private final BigDecimal totalAmount;
    private final int subscriptionCount;

    public CategoryExpenseResponse(
            Long categoryId,
            String categoryName,
            String colorCode,
            String icon,
            BigDecimal totalAmount,
            int subscriptionCount
    ) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.colorCode = colorCode;
        this.icon = icon;
        this.totalAmount = totalAmount;
        this.subscriptionCount = subscriptionCount;
    }

    public static CategoryExpenseResponse from(CategoryExpense categoryExpense) {
        return new CategoryExpenseResponse(
                categoryExpense.getCategoryId(),
                categoryExpense.getCategoryName(),
                categoryExpense.getColorCode(),
                categoryExpense.getIcon(),
                amountOrZero(categoryExpense.getTotalAmount()),
                valueOrZero(categoryExpense.getSubscriptionCount())
        );
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public int getSubscriptionCount() {
        return subscriptionCount;
    }

    private static BigDecimal amountOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
