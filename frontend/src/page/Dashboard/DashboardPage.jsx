import { Alert, TextField, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { getMe } from "../../features/auth/api/authApi";
import { useAuthStore } from "../../features/auth/store/authStore";
import {
  getCategoryExpenses,
  getDashboardSummary,
  getDashboardUpcoming,
} from "../../features/dashboard/api/dashboardApi";
import {
  formatDashboardAmount,
  formatDashboardCount,
  formatYearMonthLabel,
  getCurrentYearMonth,
} from "../../features/dashboard/utils/dashboardFormat";
import { getApiErrorMessage } from "../../shared/utils/errorMessage";
import { CategoryExpenseChart } from "./components/CategoryExpenseChart";
import { SummaryCard } from "./components/SummaryCard";
import { UpcomingList } from "./components/UpcomingList";
import styles from "./DashboardPage.module.css";

const UPCOMING_DAYS = 7;

function DashboardPage() {
  const member = useAuthStore((state) => state.member);
  const setMember = useAuthStore((state) => state.setMember);
  const [yearMonth, setYearMonth] = useState(getCurrentYearMonth);

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
  const hasDashboardError = summaryQuery.isError || upcomingQuery.isError || categoryExpensesQuery.isError;
  const dashboardError = summaryQuery.error || upcomingQuery.error || categoryExpensesQuery.error;

  return (
    <section className={styles.page}>
      <div className={styles.headerPanel}>
        <div className={styles.headerText}>
          <Typography variant="h4" component="h1" className={styles.title}>
            안녕하세요, {nickname}님
          </Typography>
          <Typography variant="body1" className={styles.description}>
            선택한 월의 구독료 흐름을 확인하세요.
          </Typography>
          <Typography variant="body2" className={styles.monthCaption}>
            구독 시작일과 결제 주기를 기준으로 계산한 예상 데이터입니다.
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
          helper="상태 이력 기준으로 선택한 월에 실제 결제 예정인 구독 수입니다."
          tone="blue"
          loading={summaryQuery.isLoading}
        />
        <SummaryCard
          label="월간 구독료 합계"
          value={formatDashboardAmount(summary.monthlyExpectedAmount)}
          helper={`${formatYearMonthLabel(summary.yearMonth || yearMonth)} 기준, 시작일·주기·상태 이력으로 계산합니다.`}
          tone="green"
          loading={summaryQuery.isLoading}
        />
        <SummaryCard
          label="현재 기준 7일 이내 예정"
          value={formatDashboardCount(summary.upcomingCount)}
          helper="오늘부터 7일 이내 결제 예정인 활성 구독입니다."
          tone="orange"
          loading={summaryQuery.isLoading}
        />
        <SummaryCard
          label="현재 기준 연체 구독"
          value={formatDashboardCount(summary.overdueCount)}
          helper="오늘 이전 결제일이 남아 있는 활성 구독입니다."
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
                가까운 결제일부터 순서대로 보여줍니다.
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
                카테고리별 월간 구독료
              </Typography>
              <Typography variant="body2" className={styles.sectionDescription}>
                구독 시작일, 결제 주기, 상태 이력을 기준으로 계산합니다.
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
    </section>
  );
}

export default DashboardPage;
