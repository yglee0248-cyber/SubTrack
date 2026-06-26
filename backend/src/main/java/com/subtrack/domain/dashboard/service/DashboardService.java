package com.subtrack.domain.dashboard.service;

import com.subtrack.domain.dashboard.dao.DashboardDao;
import com.subtrack.domain.dashboard.dto.CategoryExpenseResponse;
import com.subtrack.domain.dashboard.dto.DashboardSummaryResponse;
import com.subtrack.domain.dashboard.dto.UpcomingSubscriptionResponse;
import com.subtrack.domain.dashboard.vo.CategoryExpense;
import com.subtrack.domain.dashboard.vo.DashboardSummary;
import com.subtrack.domain.dashboard.vo.DashboardSubscription;
import com.subtrack.domain.subscription.dao.SubscriptionStatusHistoryDao;
import com.subtrack.domain.subscription.service.SubscriptionNextPaymentDateNormalizer;
import com.subtrack.domain.subscription.vo.SubscriptionStatusHistory;
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
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DashboardService {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final int DEFAULT_UPCOMING_DAYS = 7;
    private static final int MIN_UPCOMING_DAYS = 1;
    private static final int MAX_UPCOMING_DAYS = 31;
    private static final String ACTIVE_STATUS = "ACTIVE";

    private final DashboardDao dashboardDao;
    private final SubscriptionStatusHistoryDao statusHistoryDao;
    private final SubscriptionNextPaymentDateNormalizer nextPaymentDateNormalizer;

    public DashboardService(
            DashboardDao dashboardDao,
            SubscriptionStatusHistoryDao statusHistoryDao,
            SubscriptionNextPaymentDateNormalizer nextPaymentDateNormalizer
    ) {
        this.dashboardDao = dashboardDao;
        this.statusHistoryDao = statusHistoryDao;
        this.nextPaymentDateNormalizer = nextPaymentDateNormalizer;
    }

    @Transactional
    public DashboardSummaryResponse getSummary(Long memberId, String yearMonthValue) {
        nextPaymentDateNormalizer.normalizeActiveSubscriptions(memberId);
        MonthRange monthRange = resolveMonthRange(yearMonthValue);
        LocalDate today = getToday();
        List<DashboardSubscription> monthlySubscriptions = findActiveSubscriptionsOccurringInMonth(
                memberId,
                monthRange
        );

        DashboardSummary summary = new DashboardSummary();
        summary.setActiveSubscriptionCount(monthlySubscriptions.size());
        summary.setMonthlyExpectedAmount(calculateMonthlyExpectedAmount(monthlySubscriptions));
        summary.setUpcomingCount(dashboardDao.countUpcomingSubscriptions(
                memberId,
                today,
                today.plusDays(DEFAULT_UPCOMING_DAYS)
        ));
        summary.setDueTodayCount(dashboardDao.countDueTodaySubscriptions(memberId, today));

        return DashboardSummaryResponse.of(monthRange.getYearMonth(), summary);
    }

    @Transactional
    public List<UpcomingSubscriptionResponse> getUpcomingSubscriptions(Long memberId, Integer days) {
        nextPaymentDateNormalizer.normalizeActiveSubscriptions(memberId);
        int resolvedDays = resolveUpcomingDays(days);
        LocalDate today = getToday();

        return dashboardDao.findUpcomingSubscriptions(memberId, today, today.plusDays(resolvedDays))
                .stream()
                .map(UpcomingSubscriptionResponse::from)
                .toList();
    }

    @Transactional
    public List<CategoryExpenseResponse> getCategoryExpenses(Long memberId, String yearMonthValue) {
        nextPaymentDateNormalizer.normalizeActiveSubscriptions(memberId);
        MonthRange monthRange = resolveMonthRange(yearMonthValue);

        return calculateCategoryExpenses(memberId, monthRange)
                .stream()
                .map(CategoryExpenseResponse::from)
                .toList();
    }

    private BigDecimal calculateMonthlyExpectedAmount(List<DashboardSubscription> subscriptions) {
        return subscriptions
                .stream()
                .map(DashboardSubscription::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<CategoryExpense> calculateCategoryExpenses(Long memberId, MonthRange monthRange) {
        Map<Long, CategoryExpense> groupedExpenses = new LinkedHashMap<>();

        for (DashboardSubscription subscription : findActiveSubscriptionsOccurringInMonth(memberId, monthRange)) {
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

    private List<DashboardSubscription> findActiveSubscriptionsOccurringInMonth(Long memberId, MonthRange monthRange) {
        List<DashboardSubscription> candidates = dashboardDao.findRecurringDashboardSubscriptions(
                memberId,
                monthRange.getStartDate(),
                monthRange.getEndDate()
        );

        Map<Long, List<SubscriptionStatusHistory>> historiesBySubscriptionId = statusHistoryDao
                .findHistoriesForDashboard(memberId, monthRange.getStartDate(), monthRange.getEndDate())
                .stream()
                .collect(Collectors.groupingBy(SubscriptionStatusHistory::getSubscriptionId));

        return candidates.stream()
                .filter(subscription -> occursInMonthAndActiveHistory(
                        subscription,
                        monthRange.getYearMonthValue(),
                        historiesBySubscriptionId.get(subscription.getSubscriptionId())
                ))
                .toList();
    }

    private boolean occursInMonthAndActiveHistory(
            DashboardSubscription subscription,
            YearMonth yearMonth,
            List<SubscriptionStatusHistory> histories
    ) {
        LocalDate occurrenceDate = BillingDateCalculator.calculateOccurrenceDate(
                subscription.getBillingStartDate(),
                subscription.getBillingCycle(),
                yearMonth
        );

        if (occurrenceDate == null) {
            return false;
        }

        return isActiveOn(occurrenceDate, histories);
    }

    private boolean isActiveOn(LocalDate occurrenceDate, List<SubscriptionStatusHistory> histories) {
        if (histories == null || histories.isEmpty()) {
            return false;
        }

        return histories.stream()
                .anyMatch(history -> ACTIVE_STATUS.equals(history.getStatus())
                        && !occurrenceDate.isBefore(history.getEffectiveStartDate())
                        && (history.getEffectiveEndDate() == null
                        || !occurrenceDate.isAfter(history.getEffectiveEndDate())));
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
