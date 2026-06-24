package com.subtrack.domain.subscription.service;

import com.subtrack.domain.category.dao.CategoryDao;
import com.subtrack.domain.subscription.dao.SubscriptionDao;
import com.subtrack.domain.subscription.dto.SubscriptionCreateRequest;
import com.subtrack.domain.subscription.dto.SubscriptionCreateResponse;
import com.subtrack.domain.subscription.dto.SubscriptionDeleteResponse;
import com.subtrack.domain.subscription.dto.SubscriptionDetailResponse;
import com.subtrack.domain.subscription.dto.SubscriptionListResponse;
import com.subtrack.domain.subscription.dto.SubscriptionSearchCondition;
import com.subtrack.domain.subscription.dto.SubscriptionUpdateRequest;
import com.subtrack.domain.subscription.dto.SubscriptionUpdateResponse;
import com.subtrack.domain.subscription.vo.Subscription;
import com.subtrack.global.exception.BusinessException;
import com.subtrack.global.exception.ErrorCode;
import com.subtrack.global.util.PaymentStatusCalculator;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SubscriptionService {

    private static final String DEFAULT_CURRENCY = "KRW";
    private static final String DEFAULT_STATUS = "ACTIVE";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    private static final Set<String> SUBSCRIPTION_STATUSES = Set.of("ACTIVE", "PAUSED", "CANCELED");

    private final SubscriptionDao subscriptionDao;
    private final CategoryDao categoryDao;

    public SubscriptionService(SubscriptionDao subscriptionDao, CategoryDao categoryDao) {
        this.subscriptionDao = subscriptionDao;
        this.categoryDao = categoryDao;
    }

    @Transactional(readOnly = true)
    public SubscriptionListResponse getSubscriptions(
            Long memberId,
            String keyword,
            Long categoryId,
            String status,
            String paymentStatus,
            Integer page,
            Integer size
    ) {
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
        return SubscriptionListResponse.of(
                subscriptionDao.findSubscriptions(condition),
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
        return new SubscriptionCreateResponse(subscription.getSubscriptionId());
    }

    @Transactional(readOnly = true)
    public SubscriptionDetailResponse getSubscription(Long memberId, Long subscriptionId) {
        return SubscriptionDetailResponse.from(findOwnedSubscription(memberId, subscriptionId));
    }

    @Transactional
    public SubscriptionUpdateResponse updateSubscription(
            Long memberId,
            Long subscriptionId,
            SubscriptionUpdateRequest request
    ) {
        findOwnedSubscription(memberId, subscriptionId);
        validateDefaultCategory(request.getCategoryId());

        Subscription subscription = new Subscription();
        subscription.setSubscriptionId(subscriptionId);
        subscription.setMemberId(memberId);
        applyUpdateRequest(subscription, request);

        int updatedCount = subscriptionDao.updateSubscription(subscription);
        if (updatedCount == 0) {
            throwSubscriptionNotFound();
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
        subscription.setPrice(request.getPrice());
        subscription.setCurrency(resolveCurrency(request.getCurrency()));
        subscription.setBillingCycle(request.getBillingCycle());
        subscription.setBillingAnchorDay(request.getNextPaymentDate().getDayOfMonth());
        subscription.setNextPaymentDate(request.getNextPaymentDate());
        subscription.setPaymentMethod(request.getPaymentMethod());
        subscription.setMemo(request.getMemo());
        subscription.setStatus(resolveStatus(request.getStatus()));
    }

    private void applyUpdateRequest(Subscription subscription, SubscriptionUpdateRequest request) {
        subscription.setCategoryId(request.getCategoryId());
        subscription.setName(request.getName());
        subscription.setPrice(request.getPrice());
        subscription.setCurrency(resolveCurrency(request.getCurrency()));
        subscription.setBillingCycle(request.getBillingCycle());
        subscription.setBillingAnchorDay(request.getNextPaymentDate().getDayOfMonth());
        subscription.setNextPaymentDate(request.getNextPaymentDate());
        subscription.setPaymentMethod(request.getPaymentMethod());
        subscription.setMemo(request.getMemo());
        subscription.setStatus(resolveStatus(request.getStatus()));
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
        if (!StringUtils.hasText(currency)) {
            return DEFAULT_CURRENCY;
        }

        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private String resolveStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return DEFAULT_STATUS;
        }

        return status.trim().toUpperCase(Locale.ROOT);
    }

    private void throwSubscriptionNotFound() {
        throw new BusinessException(ErrorCode.NOT_FOUND, "구독을 찾을 수 없습니다.");
    }
}
