import { Alert, Button, TextField, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { getMe } from "../../features/auth/api/authApi";
import { useAuthStore } from "../../features/auth/store/authStore";
import {
  getCategoryExpenses,
  getDashboardSummary,
  getDashboardUpcoming,
  getMonthlyExpenses,
  getMonthlySchedule,
} from "../../features/dashboard/api/dashboardApi";
import {
  formatDashboardAmount,
  formatDashboardCount,
  formatYearMonthLabel,
  getCurrentYearMonth,
} from "../../features/dashboard/utils/dashboardFormat";
import { getApiErrorMessage } from "../../shared/utils/errorMessage";
import { CategoryExpenseChart } from "./components/CategoryExpenseChart";
import { MonthlyExpenseTrend } from "./components/MonthlyExpenseTrend";
import { MonthlyScheduleList } from "./components/MonthlyScheduleList";
import { SummaryCard } from "./components/SummaryCard";
import { UpcomingList } from "./components/UpcomingList";
import styles from "./DashboardPage.module.css";

const UPCOMING_DAYS = 7;
const MONTHLY_SCHEDULE_PREVIEW_COUNT = 5;

function getYearRange(yearMonth) {
  const year = (yearMonth || getCurrentYearMonth()).slice(0, 4);
  return {
    from: `${year}-01`,
    to: `${year}-12`,
  };
}

function DashboardPage() {
  const member = useAuthStore((state) => state.member);
  const setMember = useAuthStore((state) => state.setMember);
  const [yearMonth, setYearMonth] = useState(getCurrentYearMonth);
  const [isAnalyticsOpen, setIsAnalyticsOpen] = useState(false);
  const [isScheduleExpanded, setIsScheduleExpanded] = useState(false);
  const yearRange = getYearRange(yearMonth);

  const memberQuery = useQuery({
    queryKey: ["member", "me"],
    queryFn: getMe,
    staleTime: 0,
    refetchOnMount: "always",
  });

  const summaryQuery = useQuery({
    queryKey: ["dashboardSummary", yearMonth],
    queryFn: () => getDashboardSummary(yearMonth),
    staleTime: 0,
    refetchOnMount: "always",
  });

  const upcomingQuery = useQuery({
    queryKey: ["dashboardUpcoming", UPCOMING_DAYS],
    queryFn: () => getDashboardUpcoming(UPCOMING_DAYS),
    staleTime: 0,
    refetchOnMount: "always",
  });

  const categoryExpensesQuery = useQuery({
    queryKey: ["dashboardCategoryExpenses", yearMonth],
    queryFn: () => getCategoryExpenses(yearMonth),
    staleTime: 0,
    refetchOnMount: "always",
  });

  const monthlyExpensesQuery = useQuery({
    queryKey: ["dashboard", "monthlyExpenses", yearRange.from, yearRange.to],
    queryFn: () => getMonthlyExpenses(yearRange.from, yearRange.to),
    staleTime: 0,
    refetchOnMount: "always",
  });

  const monthlyScheduleQuery = useQuery({
    queryKey: ["dashboard", "monthlySchedule", yearMonth],
    queryFn: () => getMonthlySchedule(yearMonth),
    staleTime: 0,
    refetchOnMount: "always",
  });

  useEffect(() => {
    if (memberQuery.data) {
      setMember({
        memberId: memberQuery.data.memberId,
        email: memberQuery.data.email,
        nickname: memberQuery.data.nickname,
      });
    }
  }, [memberQuery.data, setMember]);

  const nickname = memberQuery.data?.nickname || member?.nickname || "사용자";
  const summary = summaryQuery.data || {};
  const categoryExpenses = categoryExpensesQuery.data || [];
  const upcomingSubscriptions = upcomingQuery.data || [];
  const monthlyScheduleItems = monthlyScheduleQuery.data?.items || [];
  const visibleMonthlyScheduleItems = isScheduleExpanded
    ? monthlyScheduleItems
    : monthlyScheduleItems.slice(0, MONTHLY_SCHEDULE_PREVIEW_COUNT);
  const visibleMonthlyScheduleData = monthlyScheduleQuery.data
    ? {
        ...monthlyScheduleQuery.data,
        items: visibleMonthlyScheduleItems,
      }
    : monthlyScheduleQuery.data;
  const hasMoreMonthlySchedule = monthlyScheduleItems.length > MONTHLY_SCHEDULE_PREVIEW_COUNT;
  const hasDashboardError =
    summaryQuery.isError ||
    upcomingQuery.isError ||
    categoryExpensesQuery.isError ||
    monthlyExpensesQuery.isError ||
    monthlyScheduleQuery.isError;
  const dashboardError =
    summaryQuery.error ||
    upcomingQuery.error ||
    categoryExpensesQuery.error ||
    monthlyExpensesQuery.error ||
    monthlyScheduleQuery.error;

  return (
    <section className={styles.page}>
      <div className={styles.headerPanel}>
        <div className={styles.headerText}>
          <span className={styles.headerBadge}>KRW 환산 기준</span>
          <Typography variant="h4" component="h1" className={styles.title}>
            안녕하세요, {nickname}님
          </Typography>
          <Typography variant="body1" className={styles.description}>
            선택 월의 구독 지출과 가까운 결제 예정 흐름을 확인하세요.
          </Typography>
        </div>

        <TextField
          label="조회 월"
          type="month"
          size="small"
          value={yearMonth}
          onChange={(event) => setYearMonth(event.target.value || getCurrentYearMonth())}
          className={styles.monthField}
          InputLabelProps={{ shrink: true }}
        />
      </div>

      {memberQuery.isError && (
        <Alert severity="warning" className={styles.stateBox}>
          내 정보를 다시 불러오지 못했습니다. 저장된 로그인 정보로 화면을 표시합니다.
        </Alert>
      )}

      {hasDashboardError && (
        <Alert severity="error" className={styles.stateBox}>
          {getApiErrorMessage(dashboardError, "대시보드 정보를 불러오지 못했습니다.")}
        </Alert>
      )}

      <div className={styles.summaryGrid}>
        <SummaryCard
          label="선택 월 결제 구독 수"
          value={formatDashboardCount(summary.activeSubscriptionCount)}
          helper="선택 월에 결제 예정인 구독"
          tone="blue"
          loading={summaryQuery.isLoading}
        />
        <SummaryCard
          label="월간 구독료 합계"
          value={formatDashboardAmount(summary.monthlyExpectedAmountKrw ?? summary.monthlyExpectedAmount)}
          helper={`${formatYearMonthLabel(summary.yearMonth || yearMonth)} 기준 예상 금액`}
          tone="green"
          loading={summaryQuery.isLoading}
        />
        <SummaryCard
          label="현재 기준 7일 이내 예정"
          value={formatDashboardCount(summary.upcomingCount)}
          helper="오늘부터 7일 이내"
          tone="orange"
          loading={summaryQuery.isLoading}
        />
        <SummaryCard
          label="오늘 결제 예정"
          value={formatDashboardCount(summary.dueTodayCount)}
          helper="오늘 결제일인 활성 구독"
          tone="red"
          loading={summaryQuery.isLoading}
        />
      </div>

      <div className={styles.contentGrid}>
        <section className={styles.panel}>
          <div className={styles.sectionHeader}>
            <div>
              <Typography variant="h6" component="h2" className={styles.sectionTitle}>
                7일 이내 결제 예정
              </Typography>
              <Typography variant="body2" className={styles.sectionDescription}>
                오늘 기준 가까운 결제부터 보여줍니다.
              </Typography>
            </div>
            {upcomingQuery.isFetching && !upcomingQuery.isLoading && (
              <span className={styles.fetchingText}>새로고침 중</span>
            )}
          </div>
          <UpcomingList items={upcomingSubscriptions} loading={upcomingQuery.isLoading} />
        </section>

        <section className={styles.panel}>
          <div className={styles.sectionHeader}>
            <div>
              <Typography variant="h6" component="h2" className={styles.sectionTitle}>
                선택 월 결제 예정
              </Typography>
              <Typography variant="body2" className={styles.sectionDescription}>
                {formatYearMonthLabel(yearMonth)}에 예정된 구독 결제 목록입니다.
              </Typography>
            </div>
            {monthlyScheduleQuery.isFetching && !monthlyScheduleQuery.isLoading && (
              <span className={styles.fetchingText}>새로고침 중</span>
            )}
          </div>
          <MonthlyScheduleList
            data={visibleMonthlyScheduleData}
            loading={monthlyScheduleQuery.isLoading}
            yearMonth={yearMonth}
          />
          {hasMoreMonthlySchedule && !monthlyScheduleQuery.isLoading && (
            <div className={styles.panelActionRow}>
              <Button variant="outlined" size="small" onClick={() => setIsScheduleExpanded((prev) => !prev)}>
                {isScheduleExpanded ? "접기" : `전체 보기 (${monthlyScheduleItems.length}개)`}
              </Button>
            </div>
          )}
        </section>
      </div>

      <section className={`${styles.panel} ${styles.analyticsPanel}`}>
        <div className={styles.analyticsSummary}>
          <div>
            <Typography variant="h6" component="h2" className={styles.sectionTitle}>
              분석 차트
            </Typography>
            <Typography variant="body2" className={styles.sectionDescription}>
              월별 추이와 카테고리별 예상 지출은 필요할 때 펼쳐서 확인하세요.
            </Typography>
          </div>
          <Button variant="contained" size="small" onClick={() => setIsAnalyticsOpen((prev) => !prev)}>
            {isAnalyticsOpen ? "분석 차트 접기" : "분석 차트 보기"}
          </Button>
        </div>

        {isAnalyticsOpen && (
          <div className={styles.analyticsGrid}>
            <section className={styles.analyticsCard}>
              <div className={styles.sectionHeader}>
                <div>
                  <Typography variant="h6" component="h3" className={styles.sectionTitle}>
                    월별 예상 구독료 추이
                  </Typography>
                  <Typography variant="body2" className={styles.sectionDescription}>
                    선택한 연도의 월별 흐름입니다.
                  </Typography>
                </div>
                {monthlyExpensesQuery.isFetching && !monthlyExpensesQuery.isLoading && (
                  <span className={styles.fetchingText}>새로고침 중</span>
                )}
              </div>
              <MonthlyExpenseTrend data={monthlyExpensesQuery.data} loading={monthlyExpensesQuery.isLoading} />
            </section>

            <section className={styles.analyticsCard}>
              <div className={styles.sectionHeader}>
                <div>
                  <Typography variant="h6" component="h3" className={styles.sectionTitle}>
                    카테고리별 월간 구독료
                  </Typography>
                  <Typography variant="body2" className={styles.sectionDescription}>
                    선택 월 예상 지출을 카테고리로 나눕니다.
                  </Typography>
                </div>
                {categoryExpensesQuery.isFetching && !categoryExpensesQuery.isLoading && (
                  <span className={styles.fetchingText}>새로고침 중</span>
                )}
              </div>
              <CategoryExpenseChart
                items={categoryExpenses}
                loading={categoryExpensesQuery.isLoading}
                yearMonth={yearMonth}
              />
            </section>
          </div>
        )}
      </section>
    </section>
  );
}

export default DashboardPage;
