package com.subtrack.domain.dashboard.service;

import com.subtrack.domain.dashboard.dao.DashboardDao;
import com.subtrack.domain.dashboard.dto.CategoryExpenseResponse;
import com.subtrack.domain.dashboard.dto.DashboardSummaryResponse;
import com.subtrack.domain.dashboard.dto.UpcomingSubscriptionResponse;
import com.subtrack.domain.dashboard.vo.CategoryExpense;
import com.subtrack.domain.dashboard.vo.DashboardSummary;
import com.subtrack.domain.dashboard.vo.DashboardSubscription;
import com.subtrack.global.exception.BusinessException;
import com.subtrack.global.exception.ErrorCode;
import com.subtrack.global.util.BillingDateCalculator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DashboardService {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final int DEFAULT_UPCOMING_DAYS = 7;
    private static final int MIN_UPCOMING_DAYS = 1;
    private static final int MAX_UPCOMING_DAYS = 31;

    private final DashboardDao dashboardDao;

    public DashboardService(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(Long memberId, String yearMonthValue) {
        MonthRange monthRange = resolveMonthRange(yearMonthValue);
        LocalDate today = getToday();

        DashboardSummary summary = new DashboardSummary();
        summary.setActiveSubscriptionCount(dashboardDao.countActiveSubscriptions(memberId));
        summary.setMonthlyExpectedAmount(calculateMonthlyExpectedAmount(memberId, monthRange));
        summary.setUpcomingCount(dashboardDao.countUpcomingSubscriptions(
                memberId,
                today,
                today.plusDays(DEFAULT_UPCOMING_DAYS)
        ));
        summary.setOverdueCount(dashboardDao.countOverdueSubscriptions(memberId, today));

        return DashboardSummaryResponse.of(monthRange.getYearMonth(), summary);
    }

    @Transactional(readOnly = true)
    public List<UpcomingSubscriptionResponse> getUpcomingSubscriptions(Long memberId, Integer days) {
        int resolvedDays = resolveUpcomingDays(days);
        LocalDate today = getToday();

        return dashboardDao.findUpcomingSubscriptions(memberId, today, today.plusDays(resolvedDays))
                .stream()
                .map(UpcomingSubscriptionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryExpenseResponse> getCategoryExpenses(Long memberId, String yearMonthValue) {
        MonthRange monthRange = resolveMonthRange(yearMonthValue);

        return calculateCategoryExpenses(memberId, monthRange)
                .stream()
                .map(CategoryExpenseResponse::from)
                .toList();
    }

    private BigDecimal calculateMonthlyExpectedAmount(Long memberId, MonthRange monthRange) {
        return findSubscriptionsOccurringInMonth(memberId, monthRange)
                .stream()
                .map(DashboardSubscription::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<CategoryExpense> calculateCategoryExpenses(Long memberId, MonthRange monthRange) {
        Map<Long, CategoryExpense> groupedExpenses = new LinkedHashMap<>();

        for (DashboardSubscription subscription : findSubscriptionsOccurringInMonth(memberId, monthRange)) {
            CategoryExpense categoryExpense = groupedExpenses.computeIfAbsent(
                    subscription.getCategoryId(),
                    categoryId -> createCategoryExpense(subscription)
            );

            categoryExpense.setTotalAmount(categoryExpense.getTotalAmount().add(subscription.getPrice()));
            categoryExpense.setSubscriptionCount(categoryExpense.getSubscriptionCount() + 1);
        }

        List<CategoryExpense> result = new ArrayList<>(groupedExpenses.values());
        result.sort(Comparator
                .comparing(CategoryExpense::getTotalAmount, Comparator.reverseOrder())
                .thenComparing(CategoryExpense::getCategoryId));

        return result;
    }

    private CategoryExpense createCategoryExpense(DashboardSubscription subscription) {
        CategoryExpense categoryExpense = new CategoryExpense();
        categoryExpense.setCategoryId(subscription.getCategoryId());
        categoryExpense.setCategoryName(subscription.getCategoryName());
        categoryExpense.setColorCode(subscription.getColorCode());
        categoryExpense.setIcon(subscription.getIcon());
        categoryExpense.setTotalAmount(BigDecimal.ZERO);
        categoryExpense.setSubscriptionCount(0);
        return categoryExpense;
    }

    private List<DashboardSubscription> findSubscriptionsOccurringInMonth(Long memberId, MonthRange monthRange) {
        return dashboardDao.findRecurringDashboardSubscriptions(
                        memberId,
                        monthRange.getStartDate(),
                        monthRange.getEndDate()
                )
                .stream()
                .filter(subscription -> occursInMonth(subscription, monthRange.getYearMonthValue()))
                .toList();
    }

    private boolean occursInMonth(DashboardSubscription subscription, YearMonth yearMonth) {
        LocalDate occurrenceDate = BillingDateCalculator.calculateOccurrenceDate(
                subscription.getBillingStartDate(),
                subscription.getBillingCycle(),
                yearMonth
        );

        if (occurrenceDate == null) {
            return false;
        }

        return subscription.getDeletedAt() == null
                || !occurrenceDate.isAfter(subscription.getDeletedAt().toLocalDate());
    }

    private MonthRange resolveMonthRange(String yearMonthValue) {
        YearMonth yearMonth;

        if (!StringUtils.hasText(yearMonthValue)) {
            yearMonth = YearMonth.now(SERVICE_ZONE);
        } else {
            yearMonth = parseYearMonth(yearMonthValue.trim());
        }

        return new MonthRange(
                yearMonth.toString(),
                yearMonth,
                yearMonth.atDay(1),
                yearMonth.atEndOfMonth()
        );
    }

    private YearMonth parseYearMonth(String yearMonthValue) {
        try {
            return YearMonth.parse(yearMonthValue);
        } catch (DateTimeParseException ex) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "yearMonth 형식은 YYYY-MM이어야 합니다.");
        }
    }

    private int resolveUpcomingDays(Integer days) {
        int resolvedDays = days == null ? DEFAULT_UPCOMING_DAYS : days;

        if (resolvedDays < MIN_UPCOMING_DAYS || resolvedDays > MAX_UPCOMING_DAYS) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "days는 1 이상 31 이하여야 합니다.");
        }

        return resolvedDays;
    }

    private LocalDate getToday() {
        return LocalDate.now(SERVICE_ZONE);
    }

    private static class MonthRange {

        private final String yearMonth;
        private final YearMonth yearMonthValue;
        private final LocalDate startDate;
        private final LocalDate endDate;

        private MonthRange(String yearMonth, YearMonth yearMonthValue, LocalDate startDate, LocalDate endDate) {
            this.yearMonth = yearMonth;
            this.yearMonthValue = yearMonthValue;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        private String getYearMonth() {
            return yearMonth;
        }

        private YearMonth getYearMonthValue() {
            return yearMonthValue;
        }

        private LocalDate getStartDate() {
            return startDate;
        }

        private LocalDate getEndDate() {
            return endDate;
        }
    }
}
