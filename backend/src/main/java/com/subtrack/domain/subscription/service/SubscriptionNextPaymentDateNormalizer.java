package com.subtrack.domain.subscription.service;

import com.subtrack.domain.subscription.dao.SubscriptionDao;
import com.subtrack.domain.subscription.vo.Subscription;
import com.subtrack.global.util.BillingDateCalculator;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionNextPaymentDateNormalizer {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final SubscriptionDao subscriptionDao;

    public SubscriptionNextPaymentDateNormalizer(SubscriptionDao subscriptionDao) {
        this.subscriptionDao = subscriptionDao;
    }

    public void normalizeActiveSubscriptions(Long memberId) {
        List<Subscription> activeSubscriptions = subscriptionDao.findActiveSubscriptionsByMemberId(memberId);
        activeSubscriptions.forEach(this::normalizeIfNeeded);
    }

    public void normalizeIfNeeded(Subscription subscription) {
        if (subscription == null || !"ACTIVE".equals(subscription.getStatus())) {
            return;
        }

        LocalDate normalizedNextPaymentDate = BillingDateCalculator.calculateNextPaymentDate(
                subscription.getBillingStartDate(),
                subscription.getBillingCycle(),
                LocalDate.now(SERVICE_ZONE)
        );

        if (normalizedNextPaymentDate.equals(subscription.getNextPaymentDate())) {
            return;
        }

        subscriptionDao.updateNextPaymentDate(
                subscription.getSubscriptionId(),
                subscription.getMemberId(),
                normalizedNextPaymentDate
        );
        subscription.setNextPaymentDate(normalizedNextPaymentDate);
    }
}
