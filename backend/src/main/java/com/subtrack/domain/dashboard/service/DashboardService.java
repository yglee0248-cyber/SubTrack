package com.subtrack.domain.dashboard.service;

import com.subtrack.domain.dashboard.dao.DashboardDao;
import com.subtrack.domain.dashboard.dto.CategoryExpenseResponse;
import com.subtrack.domain.dashboard.dto.DashboardSummaryResponse;
import com.subtrack.domain.dashboard.dto.UpcomingSubscriptionResponse;
import com.subtrack.domain.dashboard.vo.DashboardSummary;
import com.subtrack.global.exception.BusinessException;
import com.subtrack.global.exception.ErrorCode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
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

        DashboardSummary summary = dashboardDao.findSummary(
                memberId,
                monthRange.getStartDate(),
                monthRange.getEndDate(),
                today,
                today.plusDays(DEFAULT_UPCOMING_DAYS)
        );

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

        return dashboardDao.findCategoryExpenses(memberId, monthRange.getStartDate(), monthRange.getEndDate())
                .stream()
                .map(CategoryExpenseResponse::from)
                .toList();
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
        private final LocalDate startDate;
        private final LocalDate endDate;

        private MonthRange(String yearMonth, LocalDate startDate, LocalDate endDate) {
            this.yearMonth = yearMonth;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        private String getYearMonth() {
            return yearMonth;
        }

        private LocalDate getStartDate() {
            return startDate;
        }

        private LocalDate getEndDate() {
            return endDate;
        }
    }
}
