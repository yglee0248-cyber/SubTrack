package com.subtrack.global.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Set;

public final class PaymentStatusCalculator {

    public static final String UPCOMING = "UPCOMING";
    public static final String DUE_SOON = "DUE_SOON";
    public static final String DUE_TODAY = "DUE_TODAY";
    public static final String OVERDUE = "OVERDUE";

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final Set<String> PAYMENT_STATUSES = Set.of(UPCOMING, DUE_SOON, DUE_TODAY, OVERDUE);

    private PaymentStatusCalculator() {
    }

    public static String calculate(LocalDate nextPaymentDate) {
        long daysUntilPayment = ChronoUnit.DAYS.between(LocalDate.now(SERVICE_ZONE), nextPaymentDate);

        if (daysUntilPayment < 0) {
            return OVERDUE;
        }

        if (daysUntilPayment == 0) {
            return DUE_TODAY;
        }

        if (daysUntilPayment <= 7) {
            return DUE_SOON;
        }

        return UPCOMING;
    }

    public static boolean isValid(String paymentStatus) {
        return PAYMENT_STATUSES.contains(paymentStatus);
    }
}
