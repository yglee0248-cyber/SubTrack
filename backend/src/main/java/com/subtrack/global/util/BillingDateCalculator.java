package com.subtrack.global.util;

import java.time.LocalDate;
import java.time.YearMonth;

public final class BillingDateCalculator {

    private static final String MONTHLY = "MONTHLY";
    private static final String YEARLY = "YEARLY";

    private BillingDateCalculator() {
    }

    public static LocalDate calculateNextPaymentDate(
            LocalDate billingStartDate,
            String billingCycle,
            LocalDate today
    ) {
        if (billingStartDate == null || billingCycle == null || today == null) {
            throw new IllegalArgumentException("billingStartDate, billingCycle, today are required");
        }

        if (!billingStartDate.isBefore(today)) {
            return billingStartDate;
        }

        if (MONTHLY.equals(billingCycle)) {
            return calculateMonthlyNextPaymentDate(billingStartDate, today);
        }

        if (YEARLY.equals(billingCycle)) {
            return calculateYearlyNextPaymentDate(billingStartDate, today);
        }

        throw new IllegalArgumentException("Unsupported billingCycle: " + billingCycle);
    }

    public static LocalDate calculateOccurrenceDate(
            LocalDate billingStartDate,
            String billingCycle,
            YearMonth targetMonth
    ) {
        if (billingStartDate == null || billingCycle == null || targetMonth == null) {
            return null;
        }

        YearMonth startMonth = YearMonth.from(billingStartDate);

        if (MONTHLY.equals(billingCycle)) {
            if (targetMonth.isBefore(startMonth)) {
                return null;
            }

            return atAnchorDay(targetMonth, billingStartDate.getDayOfMonth());
        }

        if (YEARLY.equals(billingCycle)) {
            if (targetMonth.getYear() < billingStartDate.getYear()
                    || targetMonth.getMonthValue() != billingStartDate.getMonthValue()) {
                return null;
            }

            return atAnchorDay(targetMonth, billingStartDate.getDayOfMonth());
        }

        return null;
    }

    private static LocalDate calculateMonthlyNextPaymentDate(LocalDate billingStartDate, LocalDate today) {
        YearMonth targetMonth = YearMonth.from(today);
        LocalDate candidate = atAnchorDay(targetMonth, billingStartDate.getDayOfMonth());

        if (candidate.isBefore(today)) {
            candidate = atAnchorDay(targetMonth.plusMonths(1), billingStartDate.getDayOfMonth());
        }

        return candidate;
    }

    private static LocalDate calculateYearlyNextPaymentDate(LocalDate billingStartDate, LocalDate today) {
        YearMonth targetMonth = YearMonth.of(today.getYear(), billingStartDate.getMonthValue());
        LocalDate candidate = atAnchorDay(targetMonth, billingStartDate.getDayOfMonth());

        if (candidate.isBefore(today)) {
            candidate = atAnchorDay(targetMonth.plusYears(1), billingStartDate.getDayOfMonth());
        }

        return candidate;
    }

    private static LocalDate atAnchorDay(YearMonth yearMonth, int anchorDay) {
        return yearMonth.atDay(Math.min(anchorDay, yearMonth.lengthOfMonth()));
    }
}
