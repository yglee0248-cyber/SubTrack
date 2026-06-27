import { CircularProgress, Typography } from "@mui/material";
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import {
  formatDashboardAmount,
  formatDashboardCount,
} from "../../../features/dashboard/utils/dashboardFormat";
import styles from "../DashboardPage.module.css";
import { DashboardEmptyState } from "./DashboardEmptyState";

function buildChartData(items) {
  return items.map((item) => ({
    ...item,
    expectedAmountKrw: Number(item.expectedAmountKrw || 0),
    subscriptionCount: Number(item.subscriptionCount || 0),
    monthLabel: item.yearMonth?.slice(5) ? `${Number(item.yearMonth.slice(5))}월` : item.yearMonth,
  }));
}

export function MonthlyExpenseTrend({ data, loading }) {
  const chartData = buildChartData(data?.items || []);
  const hasAmount = chartData.some((item) => item.expectedAmountKrw > 0);

  if (loading) {
    return (
      <div className={styles.sectionLoading}>
        <CircularProgress size={26} />
        <Typography variant="body2">월별 예상 구독료를 불러오는 중입니다.</Typography>
      </div>
    );
  }

  if (!chartData.length || !hasAmount) {
    return (
      <DashboardEmptyState
        title="월별 예상 구독료 데이터가 없습니다."
        description="구독 시작일, 결제 주기, 상태 이력을 기준으로 연간 흐름을 표시합니다."
      />
    );
  }

  return (
    <div className={styles.trendChartBox}>
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={chartData} margin={{ top: 12, right: 12, bottom: 6, left: 0 }}>
          <CartesianGrid stroke="#edf0f5" vertical={false} />
          <XAxis
            dataKey="monthLabel"
            tickLine={false}
            axisLine={false}
            tick={{ fill: "#667085", fontSize: 12 }}
          />
          <YAxis
            tickLine={false}
            axisLine={false}
            tick={{ fill: "#667085", fontSize: 12 }}
            tickFormatter={(value) => Number(value).toLocaleString("ko-KR")}
            width={72}
          />
          <Tooltip
            cursor={{ fill: "rgba(31, 94, 255, 0.06)" }}
            formatter={(value, name, item) => [
              `${formatDashboardAmount(value)} / ${formatDashboardCount(item.payload.subscriptionCount)}`,
              "예상 구독료",
            ]}
            labelFormatter={(_, payload) => payload?.[0]?.payload?.yearMonth || ""}
          />
          <Bar dataKey="expectedAmountKrw" fill="#2454d6" radius={[6, 6, 0, 0]} maxBarSize={44} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
