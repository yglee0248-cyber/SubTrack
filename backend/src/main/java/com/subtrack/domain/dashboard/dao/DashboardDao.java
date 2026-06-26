package com.subtrack.domain.dashboard.dao;

import com.subtrack.domain.dashboard.vo.DashboardSubscription;
import com.subtrack.domain.dashboard.vo.UpcomingSubscription;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DashboardDao {

    int countUpcomingSubscriptions(
            @Param("memberId") Long memberId,
            @Param("today") LocalDate today,
            @Param("endDate") LocalDate endDate
    );

    int countDueTodaySubscriptions(
            @Param("memberId") Long memberId,
            @Param("today") LocalDate today
    );

    List<DashboardSubscription> findRecurringDashboardSubscriptions(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<UpcomingSubscription> findUpcomingSubscriptions(
            @Param("memberId") Long memberId,
            @Param("today") LocalDate today,
            @Param("endDate") LocalDate endDate
    );

}
