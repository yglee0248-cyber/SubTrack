package com.subtrack.domain.subscription.dao;

import com.subtrack.domain.subscription.dto.SubscriptionSearchCondition;
import com.subtrack.domain.subscription.vo.Subscription;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SubscriptionDao {

    List<Subscription> findSubscriptions(SubscriptionSearchCondition condition);

    List<Subscription> findActiveSubscriptionsByMemberId(@Param("memberId") Long memberId);

    int countSubscriptions(SubscriptionSearchCondition condition);

    Subscription findByIdAndMemberId(
            @Param("subscriptionId") Long subscriptionId,
            @Param("memberId") Long memberId
    );

    int insertSubscription(Subscription subscription);

    int updateSubscription(Subscription subscription);

    int updateNextPaymentDate(
            @Param("subscriptionId") Long subscriptionId,
            @Param("memberId") Long memberId,
            @Param("nextPaymentDate") LocalDate nextPaymentDate
    );

    int softDeleteSubscription(
            @Param("subscriptionId") Long subscriptionId,
            @Param("memberId") Long memberId
    );
}
