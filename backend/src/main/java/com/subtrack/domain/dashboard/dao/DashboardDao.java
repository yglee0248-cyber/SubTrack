package com.subtrack.domain.dashboard.dao;

import com.subtrack.domain.dashboard.vo.CategoryExpense;
import com.subtrack.domain.dashboard.vo.DashboardSummary;
import com.subtrack.domain.dashboard.vo.UpcomingSubscription;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DashboardDao {

    DashboardSummary findSummary(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("today") LocalDate today,
            @Param("upcomingEndDate") LocalDate upcomingEndDate
    );

    List<UpcomingSubscription> findUpcomingSubscriptions(
            @Param("memberId") Long memberId,
            @Param("today") LocalDate today,
            @Param("endDate") LocalDate endDate
    );

    List<CategoryExpense> findCategoryExpenses(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
