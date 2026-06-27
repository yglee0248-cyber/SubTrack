import { CircularProgress, Typography } from "@mui/material";
import {
  formatDashboardDate,
  formatYearMonthLabel,
} from "../../../features/dashboard/utils/dashboardFormat";
import { formatOriginalAndKrwAmount } from "../../../shared/utils/currencyFormat";
import styles from "../DashboardPage.module.css";
import { DashboardEmptyState } from "./DashboardEmptyState";

export function MonthlyScheduleList({ data, loading, yearMonth }) {
  const items = data?.items || [];

  if (loading) {
    return (
      <div className={styles.sectionLoading}>
        <CircularProgress size={26} />
        <Typography variant="body2">선택 월 결제 예정 목록을 불러오는 중입니다.</Typography>
      </div>
    );
  }

  if (!items.length) {
    return (
      <DashboardEmptyState
        title="선택한 월에 예정된 구독 결제가 없습니다."
        description="상태 이력 기준으로 선택 월에 결제가 발생하는 구독이 표시됩니다."
      />
    );
  }

  return (
    <ul className={styles.scheduleList} aria-label={`${formatYearMonthLabel(yearMonth)} 결제 예정 목록`}>
      {items.map((item) => (
        <li key={`${item.subscriptionId}-${item.paymentDate}`} className={styles.scheduleItem}>
          <div className={styles.scheduleDate}>
            <span>{formatDashboardDate(item.paymentDate)}</span>
          </div>
          <div className={styles.scheduleMain}>
            <Typography variant="subtitle1" component="p" className={styles.scheduleName}>
              {item.name}
            </Typography>
            <Typography variant="body2" className={styles.scheduleMeta}>
              {item.categoryName || "기타"} · {item.paymentMethod || "-"}
            </Typography>
          </div>
          <Typography variant="subtitle2" component="p" className={styles.scheduleAmount}>
            {formatOriginalAndKrwAmount(item.price, item.currency || "KRW", item.convertedPriceKrw)}
          </Typography>
        </li>
      ))}
    </ul>
  );
}
