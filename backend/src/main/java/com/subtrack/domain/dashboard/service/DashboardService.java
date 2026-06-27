package com.subtrack.domain.dashboard.service;

import com.subtrack.domain.dashboard.dao.DashboardDao;
import com.subtrack.domain.dashboard.dto.CategoryExpenseResponse;
import com.subtrack.domain.dashboard.dto.DashboardSummaryResponse;
import com.subtrack.domain.dashboard.dto.MonthlyExpenseTrendResponse;
import com.subtrack.domain.dashboard.dto.MonthlyScheduleResponse;
import com.subtrack.domain.dashboard.dto.UpcomingSubscriptionResponse;
import com.subtrack.domain.dashboard.vo.CategoryExpense;
import com.subtrack.domain.dashboard.vo.DashboardSubscription;
import com.subtrack.domain.dashboard.vo.DashboardSummary;
import com.subtrack.domain.dashboard.vo.UpcomingSubscription;
import com.subtrack.domain.exchange.dto.ExchangeRateConversion;
import com.subtrack.domain.exchange.dto.ExchangeRateQuote;
import com.subtrack.domain.exchange.service.ExchangeRateService;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
    private static final int MAX_MONTHLY_EXPENSE_RANGE = 24;
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DEFAULT_CURRENCY = "KRW";

    private final DashboardDao dashboardDao;
    private final SubscriptionStatusHistoryDao statusHistoryDao;
    private final SubscriptionNextPaymentDateNormalizer nextPaymentDateNormalizer;
    private final ExchangeRateService exchangeRateService;

    public DashboardService(
            DashboardDao dashboardDao,
            SubscriptionStatusHistoryDao statusHistoryDao,
            SubscriptionNextPaymentDateNormalizer nextPaymentDateNormalizer,
            ExchangeRateService exchangeRateService
    ) {
        this.dashboardDao = dashboardDao;
        this.statusHistoryDao = statusHistoryDao;
        this.nextPaymentDateNormalizer = nextPaymentDateNormalizer;
        this.exchangeRateService = exchangeRateService;
    }

    @Transactional
    public DashboardSummaryResponse getSummary(Long memberId, String yearMonthValue) {
        nextPaymentDateNormalizer.normalizeActiveSubscriptions(memberId);
        MonthRange monthRange = resolveMonthRange(yearMonthValue);
        LocalDate today = getToday();
        List<MonthlyOccurrence> monthlyOccurrences = findMonthlyOccurrences(memberId, monthRange);

        DashboardSummary summary = new DashboardSummary();
        summary.setActiveSubscriptionCount(monthlyOccurrences.size());
        summary.setMonthlyExpectedAmount(calculateMonthlyExpectedAmount(monthlyOccurrences));
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
        List<UpcomingSubscription> upcomingSubscriptions = dashboardDao.findUpcomingSubscriptions(
                memberId,
                today,
                today.plusDays(resolvedDays)
        );
        applyUpcomingExchangeConversions(upcomingSubscriptions);

        return upcomingSubscriptions.stream()
                .map(UpcomingSubscriptionResponse::from)
                .toList();
    }

    @Transactional
    public List<CategoryExpenseResponse> getCategoryExpenses(Long memberId, String yearMonthValue) {
        nextPaymentDateNormalizer.normalizeActiveSubscriptions(memberId);
        MonthRange monthRange = resolveMonthRange(yearMonthValue);

        return calculateCategoryExpenses(findMonthlyOccurrences(memberId, monthRange))
                .stream()
                .map(CategoryExpenseResponse::from)
                .toList();
    }

    @Transactional
    public MonthlyExpenseTrendResponse getMonthlyExpenses(Long memberId, String fromValue, String toValue) {
        YearMonth from = parseRequiredYearMonth(fromValue, "from");
        YearMonth to = parseRequiredYearMonth(toValue, "to");
        validateMonthlyExpenseRange(from, to);

        List<MonthlyExpenseTrendResponse.Item> items = new ArrayList<>();
        YearMonth cursor = from;
        while (!cursor.isAfter(to)) {
            MonthRange monthRange = toMonthRange(cursor);
            List<MonthlyOccurrence> occurrences = findMonthlyOccurrences(memberId, monthRange);
            items.add(new MonthlyExpenseTrendResponse.Item(
                    monthRange.getYearMonth(),
                    calculateMonthlyExpectedAmount(occurrences),
                    occurrences.size()
            ));
            cursor = cursor.plusMonths(1);
        }

        return new MonthlyExpenseTrendResponse(from.toString(), to.toString(), items);
    }

    @Transactional
    public MonthlyScheduleResponse getMonthlySchedule(Long memberId, String yearMonthValue) {
        YearMonth yearMonth = parseRequiredYearMonth(yearMonthValue, "yearMonth");
        MonthRange monthRange = toMonthRange(yearMonth);
        List<MonthlyScheduleResponse.Item> items = findMonthlyOccurrences(memberId, monthRange)
                .stream()
                .sorted(Comparator
                        .comparing(MonthlyOccurrence::getOccurrenceDate)
                        .thenComparing(occurrence -> occurrence.getSubscription().getName())
                        .thenComparing(occurrence -> occurrence.getSubscription().getSubscriptionId()))
                .map(occurrence -> new MonthlyScheduleResponse.Item(
                        occurrence.getSubscription(),
                        occurrence.getOccurrenceDate()
                ))
                .toList();

        return new MonthlyScheduleResponse(monthRange.getYearMonth(), items);
    }

    private BigDecimal calculateMonthlyExpectedAmount(List<MonthlyOccurrence> occurrences) {
        return occurrences
                .stream()
                .map(occurrence -> occurrence.getSubscription().getConvertedPriceKrw())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<CategoryExpense> calculateCategoryExpenses(List<MonthlyOccurrence> occurrences) {
        Map<Long, CategoryExpense> groupedExpenses = new LinkedHashMap<>();

        for (MonthlyOccurrence occurrence : occurrences) {
            DashboardSubscription subscription = occurrence.getSubscription();
            CategoryExpense categoryExpense = groupedExpenses.computeIfAbsent(
                    subscription.getCategoryId(),
                    categoryId -> createCategoryExpense(subscription)
            );

            categoryExpense.setTotalAmount(categoryExpense.getTotalAmount().add(subscription.getConvertedPriceKrw()));
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

    private List<MonthlyOccurrence> findMonthlyOccurrences(Long memberId, MonthRange monthRange) {
        List<DashboardSubscription> candidates = dashboardDao.findRecurringDashboardSubscriptions(
                memberId,
                monthRange.getStartDate(),
                monthRange.getEndDate()
        );
        applyDashboardExchangeConversions(candidates);

        Map<Long, List<SubscriptionStatusHistory>> historiesBySubscriptionId = statusHistoryDao
                .findHistoriesForDashboard(memberId, monthRange.getStartDate(), monthRange.getEndDate())
                .stream()
                .collect(Collectors.groupingBy(SubscriptionStatusHistory::getSubscriptionId));

        return candidates.stream()
                .map(subscription -> toMonthlyOccurrence(
                        subscription,
                        monthRange.getYearMonthValue(),
                        historiesBySubscriptionId.get(subscription.getSubscriptionId())
                ))
                .filter(occurrence -> occurrence != null)
                .toList();
    }

    private MonthlyOccurrence toMonthlyOccurrence(
            DashboardSubscription subscription,
            YearMonth yearMonth,
            List<SubscriptionStatusHistory> histories
    ) {
        LocalDate occurrenceDate = BillingDateCalculator.calculateOccurrenceDate(
                subscription.getBillingStartDate(),
                subscription.getBillingCycle(),
                yearMonth
        );

        if (occurrenceDate == null || !isActiveOn(occurrenceDate, histories)) {
            return null;
        }

        return new MonthlyOccurrence(subscription, occurrenceDate);
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

    private void applyDashboardExchangeConversions(Collection<DashboardSubscription> subscriptions) {
        if (subscriptions == null || subscriptions.isEmpty()) {
            return;
        }

        Map<String, ExchangeRateQuote> rateMap = exchangeRateService.getRatesToKrw(
                subscriptions.stream()
                        .map(subscription -> resolveCurrency(subscription.getCurrency()))
                        .collect(Collectors.toSet())
        );

        for (DashboardSubscription subscription : subscriptions) {
            String currency = resolveCurrency(subscription.getCurrency());
            subscription.setCurrency(currency);
            ExchangeRateQuote quote = rateMap.get(currency);
            ExchangeRateConversion conversion = exchangeRateService.convertToKrw(subscription.getPrice(), quote);
            subscription.setRateToKrw(conversion.getRateToKrw());
            subscription.setConvertedPriceKrw(conversion.getConvertedPriceKrw());
            subscription.setExchangeRateDate(conversion.getExchangeRateDate());
        }
    }

    private void applyUpcomingExchangeConversions(Collection<UpcomingSubscription> subscriptions) {
        if (subscriptions == null || subscriptions.isEmpty()) {
            return;
        }

        Map<String, ExchangeRateQuote> rateMap = exchangeRateService.getRatesToKrw(
                subscriptions.stream()
                        .map(subscription -> resolveCurrency(subscription.getCurrency()))
                        .collect(Collectors.toSet())
        );

        for (UpcomingSubscription subscription : subscriptions) {
            String currency = resolveCurrency(subscription.getCurrency());
            subscription.setCurrency(currency);
            ExchangeRateQuote quote = rateMap.get(currency);
            ExchangeRateConversion conversion = exchangeRateService.convertToKrw(subscription.getPrice(), quote);
            subscription.setRateToKrw(conversion.getRateToKrw());
            subscription.setConvertedPriceKrw(conversion.getConvertedPriceKrw());
            subscription.setExchangeRateDate(conversion.getExchangeRateDate());
        }
    }

    private MonthRange resolveMonthRange(String yearMonthValue) {
        YearMonth yearMonth;

        if (!StringUtils.hasText(yearMonthValue)) {
            yearMonth = YearMonth.now(SERVICE_ZONE);
        } else {
            yearMonth = parseYearMonth(yearMonthValue.trim(), "yearMonth");
        }

        return toMonthRange(yearMonth);
    }

    private YearMonth parseRequiredYearMonth(String yearMonthValue, String fieldName) {
        if (!StringUtils.hasText(yearMonthValue)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, fieldName + " 값은 필수입니다.");
        }

        return parseYearMonth(yearMonthValue.trim(), fieldName);
    }

    private YearMonth parseYearMonth(String yearMonthValue, String fieldName) {
        try {
            return YearMonth.parse(yearMonthValue);
        } catch (DateTimeParseException ex) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, fieldName + " 형식은 YYYY-MM이어야 합니다.");
        }
    }

    private MonthRange toMonthRange(YearMonth yearMonth) {
        return new MonthRange(
                yearMonth.toString(),
                yearMonth,
                yearMonth.atDay(1),
                yearMonth.atEndOfMonth()
        );
    }

    private void validateMonthlyExpenseRange(YearMonth from, YearMonth to) {
        if (from.isAfter(to)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "from은 to보다 이후일 수 없습니다.");
        }

        long monthCount = ChronoUnit.MONTHS.between(from, to) + 1;
        if (monthCount > MAX_MONTHLY_EXPENSE_RANGE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "조회 범위는 최대 24개월입니다.");
        }
    }

    private int resolveUpcomingDays(Integer days) {
        int resolvedDays = days == null ? DEFAULT_UPCOMING_DAYS : days;

        if (resolvedDays < MIN_UPCOMING_DAYS || resolvedDays > MAX_UPCOMING_DAYS) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "days는 1 이상 31 이하여야 합니다.");
        }

        return resolvedDays;
    }

    private String resolveCurrency(String currency) {
        String resolvedCurrency = StringUtils.hasText(currency)
                ? currency.trim().toUpperCase(Locale.ROOT)
                : DEFAULT_CURRENCY;
        exchangeRateService.validateSupportedCurrency(resolvedCurrency);
        return resolvedCurrency;
    }

    private LocalDate getToday() {
        return LocalDate.now(SERVICE_ZONE);
    }

    private static class MonthlyOccurrence {

        private final DashboardSubscription subscription;
        private final LocalDate occurrenceDate;

        private MonthlyOccurrence(DashboardSubscription subscription, LocalDate occurrenceDate) {
            this.subscription = subscription;
            this.occurrenceDate = occurrenceDate;
        }

        private DashboardSubscription getSubscription() {
            return subscription;
        }

        private LocalDate getOccurrenceDate() {
            return occurrenceDate;
        }
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
