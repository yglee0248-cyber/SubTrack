import { CircularProgress, Typography } from "@mui/material";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import {
  formatDashboardAmount,
  formatYearMonthLabel,
} from "../../../features/dashboard/utils/dashboardFormat";
import styles from "../DashboardPage.module.css";
import { DashboardEmptyState } from "./DashboardEmptyState";

function buildChartData(items) {
  return items.map((item) => ({
    ...item,
    categoryName: item.categoryName || "기타",
    totalAmount: Number(item.totalAmount || 0),
    subscriptionCount: Number(item.subscriptionCount || 0),
    colorCode: item.colorCode || "#4f6bff",
  }));
}

export function CategoryExpenseChart({ items, loading, yearMonth }) {
  const chartData = buildChartData(items);

  if (loading) {
    return (
      <div className={styles.sectionLoading}>
        <CircularProgress size={26} />
        <Typography variant="body2">카테고리 지출을 불러오는 중입니다.</Typography>
      </div>
    );
  }

  if (!chartData.length) {
    return (
      <DashboardEmptyState
        title={`${formatYearMonthLabel(yearMonth)} 예상 지출이 없습니다.`}
        description="활성 구독의 다음 결제일이 선택한 월에 들어오면 차트가 표시됩니다."
      />
    );
  }

  return (
    <div className={styles.chartContent}>
      <div className={styles.chartBox}>
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chartData} layout="vertical" margin={{ top: 8, right: 8, bottom: 8, left: 0 }}>
            <CartesianGrid stroke="#edf0f5" horizontal={false} />
            <XAxis
              type="number"
              tickLine={false}
              axisLine={false}
              tick={{ fill: "#667085", fontSize: 12 }}
              tickFormatter={(value) => Number(value).toLocaleString("ko-KR")}
            />
            <YAxis
              type="category"
              dataKey="categoryName"
              tickLine={false}
              axisLine={false}
              tick={{ fill: "#667085", fontSize: 12 }}
              tickFormatter={(value) => (value.length > 8 ? `${value.slice(0, 8)}...` : value)}
              width={86}
            />
            <Tooltip
              cursor={{ fill: "rgba(31, 94, 255, 0.06)" }}
              formatter={(value) => [formatDashboardAmount(value), "예상 금액"]}
              labelFormatter={(label) => `${label} 카테고리`}
            />
            <Bar dataKey="totalAmount" radius={[6, 6, 0, 0]} maxBarSize={44}>
              {chartData.map((item) => (
                <Cell key={item.categoryId} fill={item.colorCode} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>

      <ul className={styles.categoryList}>
        {chartData.map((item) => (
          <li key={item.categoryId} className={styles.categoryItem}>
            <span
              className={styles.categorySwatch}
              style={{ backgroundColor: item.colorCode }}
              aria-hidden="true"
            />
            <span className={styles.categoryName}>{item.categoryName}</span>
            <span className={styles.categoryCount}>{item.subscriptionCount}개</span>
            <strong className={styles.categoryAmount}>{formatDashboardAmount(item.totalAmount)}</strong>
          </li>
        ))}
      </ul>
    </div>
  );
}
