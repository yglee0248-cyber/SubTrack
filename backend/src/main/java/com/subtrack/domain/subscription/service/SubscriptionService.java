package com.subtrack.domain.subscription.service;

import com.subtrack.domain.category.dao.CategoryDao;
import com.subtrack.domain.exchange.dto.ExchangeRateConversion;
import com.subtrack.domain.exchange.dto.ExchangeRateQuote;
import com.subtrack.domain.exchange.service.ExchangeRateService;
import com.subtrack.domain.subscription.dao.SubscriptionDao;
import com.subtrack.domain.subscription.dao.SubscriptionStatusHistoryDao;
import com.subtrack.domain.subscription.dto.SubscriptionCreateRequest;
import com.subtrack.domain.subscription.dto.SubscriptionCreateResponse;
import com.subtrack.domain.subscription.dto.SubscriptionDeleteResponse;
import com.subtrack.domain.subscription.dto.SubscriptionDetailResponse;
import com.subtrack.domain.subscription.dto.SubscriptionListResponse;
import com.subtrack.domain.subscription.dto.SubscriptionSearchCondition;
import com.subtrack.domain.subscription.dto.SubscriptionUpdateRequest;
import com.subtrack.domain.subscription.dto.SubscriptionUpdateResponse;
import com.subtrack.domain.subscription.vo.Subscription;
import com.subtrack.domain.subscription.vo.SubscriptionStatusHistory;
import com.subtrack.global.exception.BusinessException;
import com.subtrack.global.exception.ErrorCode;
import com.subtrack.global.util.BillingDateCalculator;
import com.subtrack.global.util.PaymentStatusCalculator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SubscriptionService {

    private static final String DEFAULT_CURRENCY = "KRW";
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String PAUSED_STATUS = "PAUSED";
    private static final String CANCELED_STATUS = "CANCELED";
    private static final String DEFAULT_STATUS = ACTIVE_STATUS;
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final Set<String> SUBSCRIPTION_STATUSES = Set.of(ACTIVE_STATUS, PAUSED_STATUS, CANCELED_STATUS);

    private final SubscriptionDao subscriptionDao;
    private final CategoryDao categoryDao;
    private final SubscriptionStatusHistoryDao statusHistoryDao;
    private final SubscriptionNextPaymentDateNormalizer nextPaymentDateNormalizer;
    private final ExchangeRateService exchangeRateService;

    public SubscriptionService(
            SubscriptionDao subscriptionDao,
            CategoryDao categoryDao,
            SubscriptionStatusHistoryDao statusHistoryDao,
            SubscriptionNextPaymentDateNormalizer nextPaymentDateNormalizer,
            ExchangeRateService exchangeRateService
    ) {
        this.subscriptionDao = subscriptionDao;
        this.categoryDao = categoryDao;
        this.statusHistoryDao = statusHistoryDao;
        this.nextPaymentDateNormalizer = nextPaymentDateNormalizer;
        this.exchangeRateService = exchangeRateService;
    }

    @Transactional
    public SubscriptionListResponse getSubscriptions(
            Long memberId,
            String keyword,
            Long categoryId,
            String status,
            String paymentStatus,
            Integer page,
            Integer size
    ) {
        nextPaymentDateNormalizer.normalizeActiveSubscriptions(memberId);
        SubscriptionSearchCondition condition = buildSearchCondition(
                memberId,
                keyword,
                categoryId,
                status,
                paymentStatus,
                page,
                size
        );

        int totalCount = subscriptionDao.countSubscriptions(condition);
        List<Subscription> subscriptions = subscriptionDao.findSubscriptions(condition);
        applyExchangeConversions(subscriptions);

        return SubscriptionListResponse.of(
                subscriptions,
                condition.getPage(),
                condition.getSize(),
                totalCount
        );
    }

    @Transactional
    public SubscriptionCreateResponse createSubscription(Long memberId, SubscriptionCreateRequest request) {
        validateDefaultCategory(request.getCategoryId());

        Subscription subscription = new Subscription();
        subscription.setMemberId(memberId);
        applyCreateRequest(subscription, request);

        subscriptionDao.insertSubscription(subscription);
        createInitialStatusHistories(subscription, request.getStatusEffectiveDate());
        return new SubscriptionCreateResponse(subscription.getSubscriptionId());
    }

    @Transactional
    public SubscriptionDetailResponse getSubscription(Long memberId, Long subscriptionId) {
        Subscription subscription = findOwnedSubscription(memberId, subscriptionId);
        nextPaymentDateNormalizer.normalizeIfNeeded(subscription);
        applyExchangeConversion(subscription);
        return SubscriptionDetailResponse.from(subscription);
    }

    @Transactional
    public SubscriptionUpdateResponse updateSubscription(
            Long memberId,
            Long subscriptionId,
            SubscriptionUpdateRequest request
    ) {
        Subscription existingSubscription = findOwnedSubscription(memberId, subscriptionId);
        validateDefaultCategory(request.getCategoryId());
        String nextStatus = resolveStatus(request.getStatus());

        Subscription subscription = new Subscription();
        subscription.setSubscriptionId(subscriptionId);
        subscription.setMemberId(memberId);
        applyUpdateRequest(subscription, request);

        int updatedCount = subscriptionDao.updateSubscription(subscription);
        if (updatedCount == 0) {
            throwSubscriptionNotFound();
        }

        if (!nextStatus.equals(existingSubscription.getStatus())) {
            changeStatusHistory(existingSubscription, request, nextStatus);
        } else {
            updateInactiveStatusEffectiveDateIfNeeded(existingSubscription, request, nextStatus);
        }

        return new SubscriptionUpdateResponse(subscriptionId);
    }

    @Transactional
    public SubscriptionDeleteResponse deleteSubscription(Long memberId, Long subscriptionId) {
        findOwnedSubscription(memberId, subscriptionId);

        int deletedCount = subscriptionDao.softDeleteSubscription(subscriptionId, memberId);
        if (deletedCount == 0) {
            throwSubscriptionNotFound();
        }

        return new SubscriptionDeleteResponse(subscriptionId, true);
    }

    private SubscriptionSearchCondition buildSearchCondition(
            Long memberId,
            String keyword,
            Long categoryId,
            String status,
            String paymentStatus,
            Integer page,
            Integer size
    ) {
        int resolvedPage = page == null ? DEFAULT_PAGE : page;
        int resolvedSize = size == null ? DEFAULT_SIZE : size;

        if (resolvedPage < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "page는 0 이상이어야 합니다.");
        }

        if (resolvedSize < 1 || resolvedSize > MAX_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "size는 1 이상 100 이하여야 합니다.");
        }

        return new SubscriptionSearchCondition(
                memberId,
                normalizeKeyword(keyword),
                categoryId,
                normalizeSubscriptionStatus(status),
                normalizePaymentStatus(paymentStatus),
                resolvedPage,
                resolvedSize
        );
    }

    private void applyCreateRequest(Subscription subscription, SubscriptionCreateRequest request) {
        subscription.setCategoryId(request.getCategoryId());
        subscription.setName(request.getName());
        applyMoney(subscription, request.getPrice(), request.getCurrency());
        subscription.setBillingCycle(request.getBillingCycle());
        applyBillingDates(subscription, request.getBillingStartDate(), request.getBillingCycle());
        subscription.setPaymentMethod(request.getPaymentMethod());
        subscription.setMemo(request.getMemo());
        subscription.setStatus(resolveStatus(request.getStatus()));
    }

    private void applyUpdateRequest(Subscription subscription, SubscriptionUpdateRequest request) {
        subscription.setCategoryId(request.getCategoryId());
        subscription.setName(request.getName());
        applyMoney(subscription, request.getPrice(), request.getCurrency());
        subscription.setBillingCycle(request.getBillingCycle());
        applyBillingDates(subscription, request.getBillingStartDate(), request.getBillingCycle());
        subscription.setPaymentMethod(request.getPaymentMethod());
        subscription.setMemo(request.getMemo());
        subscription.setStatus(resolveStatus(request.getStatus()));
    }

    private void applyBillingDates(Subscription subscription, LocalDate billingStartDate, String billingCycle) {
        subscription.setBillingStartDate(billingStartDate);
        subscription.setBillingAnchorDay(billingStartDate.getDayOfMonth());
        subscription.setNextPaymentDate(BillingDateCalculator.calculateNextPaymentDate(
                billingStartDate,
                billingCycle,
                LocalDate.now(SERVICE_ZONE)
        ));
    }

    private void applyMoney(Subscription subscription, BigDecimal price, String requestedCurrency) {
        String currency = resolveCurrency(requestedCurrency);
        exchangeRateService.validatePriceScale(price, currency);
        subscription.setPrice(price);
        subscription.setCurrency(currency);
    }

    private void applyExchangeConversions(Collection<Subscription> subscriptions) {
        if (subscriptions == null || subscriptions.isEmpty()) {
            return;
        }

        Map<String, ExchangeRateQuote> rateMap = exchangeRateService.getRatesToKrw(
                subscriptions.stream()
                        .map(subscription -> resolveCurrency(subscription.getCurrency()))
                        .collect(Collectors.toSet())
        );

        for (Subscription subscription : subscriptions) {
            String currency = resolveCurrency(subscription.getCurrency());
            subscription.setCurrency(currency);
            applyExchangeConversion(subscription, rateMap.get(currency));
        }
    }

    private void applyExchangeConversion(Subscription subscription) {
        if (subscription == null) {
            return;
        }

        String currency = resolveCurrency(subscription.getCurrency());
        subscription.setCurrency(currency);
        ExchangeRateQuote quote = exchangeRateService.getRateToKrw(currency);
        applyExchangeConversion(subscription, quote);
    }

    private void applyExchangeConversion(Subscription subscription, ExchangeRateQuote quote) {
        if (subscription == null || quote == null) {
            return;
        }

        ExchangeRateConversion conversion = exchangeRateService.convertToKrw(subscription.getPrice(), quote);
        subscription.setRateToKrw(conversion.getRateToKrw());
        subscription.setConvertedPriceKrw(conversion.getConvertedPriceKrw());
        subscription.setExchangeRateDate(conversion.getExchangeRateDate());
    }

    private void createInitialStatusHistories(Subscription subscription, LocalDate requestedEffectiveDate) {
        String status = subscription.getStatus();
        LocalDate billingStartDate = subscription.getBillingStartDate();

        if (ACTIVE_STATUS.equals(status)) {
            insertStatusHistory(subscription.getSubscriptionId(), ACTIVE_STATUS, billingStartDate, null);
            return;
        }

        LocalDate effectiveDate = resolveInactiveStatusEffectiveDate(billingStartDate, requestedEffectiveDate);
        if (effectiveDate.isAfter(billingStartDate)) {
            insertStatusHistory(
                    subscription.getSubscriptionId(),
                    ACTIVE_STATUS,
                    billingStartDate,
                    effectiveDate.minusDays(1)
            );
            insertStatusHistory(subscription.getSubscriptionId(), status, effectiveDate, null);
            return;
        }

        insertStatusHistory(subscription.getSubscriptionId(), status, billingStartDate, null);
    }

    private void changeStatusHistory(
            Subscription existingSubscription,
            SubscriptionUpdateRequest request,
            String nextStatus
    ) {
        LocalDate effectiveDate = resolveStatusChangeEffectiveDate(
                nextStatus,
                request.getBillingStartDate(),
                request.getStatusEffectiveDate()
        );
        SubscriptionStatusHistory openHistory = statusHistoryDao.findOpenHistoryBySubscriptionId(
                existingSubscription.getSubscriptionId()
        );

        if (openHistory == null) {
            insertStatusHistory(existingSubscription.getSubscriptionId(), nextStatus, effectiveDate, null);
            return;
        }

        if (effectiveDate.isBefore(openHistory.getEffectiveStartDate())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "상태 적용일은 현재 상태 이력 시작일보다 이전일 수 없습니다.");
        }

        if (effectiveDate.equals(openHistory.getEffectiveStartDate())) {
            statusHistoryDao.updateOpenHistoryStatus(existingSubscription.getSubscriptionId(), nextStatus);
            return;
        }

        statusHistoryDao.closeOpenHistory(existingSubscription.getSubscriptionId(), effectiveDate.minusDays(1));
        insertStatusHistory(existingSubscription.getSubscriptionId(), nextStatus, effectiveDate, null);
    }

    private void updateInactiveStatusEffectiveDateIfNeeded(
            Subscription existingSubscription,
            SubscriptionUpdateRequest request,
            String nextStatus
    ) {
        if (ACTIVE_STATUS.equals(nextStatus)) {
            return;
        }

        LocalDate effectiveDate = resolveInactiveStatusEffectiveDate(
                request.getBillingStartDate(),
                request.getStatusEffectiveDate()
        );
        SubscriptionStatusHistory openHistory = statusHistoryDao.findOpenHistoryBySubscriptionId(
                existingSubscription.getSubscriptionId()
        );

        if (openHistory == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "현재 상태 이력을 찾을 수 없습니다.");
        }

        if (!nextStatus.equals(openHistory.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "현재 상태 이력과 요청 상태가 일치하지 않습니다.");
        }

        if (effectiveDate.equals(openHistory.getEffectiveStartDate())) {
            return;
        }

        SubscriptionStatusHistory previousHistory = findPreviousHistory(
                existingSubscription.getSubscriptionId(),
                openHistory.getStatusHistoryId()
        );

        if (previousHistory != null) {
            LocalDate previousEndDate = effectiveDate.minusDays(1);
            if (previousEndDate.isBefore(previousHistory.getEffectiveStartDate())) {
                throw new BusinessException(
                        ErrorCode.VALIDATION_ERROR,
                        "상태 적용일이 이전 상태 이력 시작일과 충돌합니다."
                );
            }

            statusHistoryDao.updateHistoryEndDate(
                    previousHistory.getStatusHistoryId(),
                    previousEndDate
            );
        }

        int updatedCount = statusHistoryDao.updateHistoryStartDate(openHistory.getStatusHistoryId(), effectiveDate);
        if (updatedCount == 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "상태 적용일을 수정하지 못했습니다.");
        }
    }

    private SubscriptionStatusHistory findPreviousHistory(Long subscriptionId, Long openStatusHistoryId) {
        List<SubscriptionStatusHistory> histories = statusHistoryDao.findHistoriesBySubscriptionId(subscriptionId);
        SubscriptionStatusHistory previousHistory = null;

        for (SubscriptionStatusHistory history : histories) {
            if (history.getStatusHistoryId().equals(openStatusHistoryId)) {
                return previousHistory;
            }

            previousHistory = history;
        }

        return previousHistory;
    }

    private LocalDate resolveStatusChangeEffectiveDate(
            String nextStatus,
            LocalDate billingStartDate,
            LocalDate requestedEffectiveDate
    ) {
        if (ACTIVE_STATUS.equals(nextStatus)) {
            return resolveActiveStatusEffectiveDate(billingStartDate);
        }

        return resolveInactiveStatusEffectiveDate(billingStartDate, requestedEffectiveDate);
    }

    private LocalDate resolveActiveStatusEffectiveDate(LocalDate billingStartDate) {
        LocalDate today = getToday();
        return billingStartDate.isAfter(today) ? billingStartDate : today;
    }

    private LocalDate resolveInactiveStatusEffectiveDate(LocalDate billingStartDate, LocalDate requestedEffectiveDate) {
        if (requestedEffectiveDate == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "상태 적용일은 필수입니다.");
        }

        LocalDate effectiveDate = requestedEffectiveDate;

        if (effectiveDate.isBefore(billingStartDate)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "상태 적용일은 구독 시작일보다 이전일 수 없습니다.");
        }

        if (effectiveDate.isAfter(getToday())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "미래 날짜의 상태 변경 예약은 아직 지원하지 않습니다.");
        }

        return effectiveDate;
    }

    private void insertStatusHistory(
            Long subscriptionId,
            String status,
            LocalDate effectiveStartDate,
            LocalDate effectiveEndDate
    ) {
        SubscriptionStatusHistory history = new SubscriptionStatusHistory();
        history.setSubscriptionId(subscriptionId);
        history.setStatus(status);
        history.setEffectiveStartDate(effectiveStartDate);
        history.setEffectiveEndDate(effectiveEndDate);
        statusHistoryDao.insertHistory(history);
    }

    private Subscription findOwnedSubscription(Long memberId, Long subscriptionId) {
        Subscription subscription = subscriptionDao.findByIdAndMemberId(subscriptionId, memberId);

        if (subscription == null) {
            throwSubscriptionNotFound();
        }

        return subscription;
    }

    private void validateDefaultCategory(Long categoryId) {
        if (categoryDao.existsDefaultCategoryById(categoryId) == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다.");
        }
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        return keyword.trim();
    }

    private String normalizeSubscriptionStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }

        String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
        if (!SUBSCRIPTION_STATUSES.contains(normalizedStatus)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "status 값이 올바르지 않습니다.");
        }

        return normalizedStatus;
    }

    private String normalizePaymentStatus(String paymentStatus) {
        if (!StringUtils.hasText(paymentStatus)) {
            return null;
        }

        String normalizedPaymentStatus = paymentStatus.trim().toUpperCase(Locale.ROOT);
        if (!PaymentStatusCalculator.isValid(normalizedPaymentStatus)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "paymentStatus 값이 올바르지 않습니다.");
        }

        return normalizedPaymentStatus;
    }

    private String resolveCurrency(String currency) {
        String resolvedCurrency = StringUtils.hasText(currency)
                ? currency.trim().toUpperCase(Locale.ROOT)
                : DEFAULT_CURRENCY;
        exchangeRateService.validateSupportedCurrency(resolvedCurrency);
        return resolvedCurrency;
    }

    private String resolveStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return DEFAULT_STATUS;
        }

        String resolvedStatus = status.trim().toUpperCase(Locale.ROOT);
        if (!SUBSCRIPTION_STATUSES.contains(resolvedStatus)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "status 값이 올바르지 않습니다.");
        }

        return resolvedStatus;
    }

    private LocalDate getToday() {
        return LocalDate.now(SERVICE_ZONE);
    }

    private void throwSubscriptionNotFound() {
        throw new BusinessException(ErrorCode.NOT_FOUND, "구독을 찾을 수 없습니다.");
    }
}
