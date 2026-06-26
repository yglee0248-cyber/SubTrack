package com.subtrack.domain.subscription.dao;

import com.subtrack.domain.subscription.vo.SubscriptionStatusHistory;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SubscriptionStatusHistoryDao {

    SubscriptionStatusHistory findOpenHistoryBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

    List<SubscriptionStatusHistory> findHistoriesBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

    List<SubscriptionStatusHistory> findHistoriesForDashboard(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    int insertHistory(SubscriptionStatusHistory history);

    int closeOpenHistory(
            @Param("subscriptionId") Long subscriptionId,
            @Param("effectiveEndDate") LocalDate effectiveEndDate
    );

    int updateOpenHistoryStatus(
            @Param("subscriptionId") Long subscriptionId,
            @Param("status") String status
    );
}
