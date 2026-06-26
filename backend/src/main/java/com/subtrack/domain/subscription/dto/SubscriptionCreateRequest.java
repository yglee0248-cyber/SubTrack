package com.subtrack.domain.subscription.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

public class SubscriptionCreateRequest {

    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;

    @NotBlank(message = "구독 이름은 필수입니다.")
    @Size(max = 100, message = "구독 이름은 100자 이하여야 합니다.")
    private String name;

    @NotNull(message = "가격은 필수입니다.")
    @DecimalMin(value = "0.00", message = "가격은 0 이상이어야 합니다.")
    private BigDecimal price;

    @Size(min = 3, max = 3, message = "통화 코드는 3자여야 합니다.")
    private String currency;

    @NotBlank(message = "결제 주기는 필수입니다.")
    @Pattern(regexp = "MONTHLY|YEARLY", message = "결제 주기는 MONTHLY 또는 YEARLY만 가능합니다.")
    private String billingCycle;

    @JsonAlias("nextPaymentDate")
    @NotNull(message = "구독 시작일 또는 첫 결제일은 필수입니다.")
    private LocalDate billingStartDate;

    @NotBlank(message = "결제 수단은 필수입니다.")
    @Size(max = 30, message = "결제 수단은 30자 이하여야 합니다.")
    private String paymentMethod;

    @Size(max = 500, message = "메모는 500자 이하여야 합니다.")
    private String memo;

    @Pattern(regexp = "ACTIVE|PAUSED|CANCELED", message = "상태는 ACTIVE, PAUSED, CANCELED만 가능합니다.")
    private String status;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = normalizeUppercase(currency);
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = normalizeUppercase(billingCycle);
    }

    public LocalDate getBillingStartDate() {
        return billingStartDate;
    }

    public void setBillingStartDate(LocalDate billingStartDate) {
        this.billingStartDate = billingStartDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod == null ? null : paymentMethod.trim();
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = normalizeUppercase(status);
    }

    private String normalizeUppercase(String value) {
        if (value == null) {
            return null;
        }

        return value.trim().toUpperCase(Locale.ROOT);
    }
}
