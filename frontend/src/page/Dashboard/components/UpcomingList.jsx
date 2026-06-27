import { CircularProgress, Typography } from "@mui/material";
import {
  formatDashboardDate,
  getDashboardPaymentStatusLabel,
  getDashboardPaymentStatusTone,
} from "../../../features/dashboard/utils/dashboardFormat";
import { formatOriginalAndKrwAmount } from "../../../shared/utils/currencyFormat";
import styles from "../DashboardPage.module.css";
import { DashboardEmptyState } from "./DashboardEmptyState";

export function UpcomingList({ items, loading }) {
  if (loading) {
    return (
      <div className={styles.sectionLoading}>
        <CircularProgress size={26} />
        <Typography variant="body2">결제 예정 구독을 불러오는 중입니다.</Typography>
      </div>
    );
  }

  if (!items.length) {
    return (
      <DashboardEmptyState
        title="7일 이내 결제 예정 구독이 없습니다."
        description="다가오는 결제가 생기면 이곳에서 바로 확인할 수 있습니다."
      />
    );
  }

  return (
    <ul className={styles.upcomingList}>
      {items.map((item) => {
        const paymentTone = getDashboardPaymentStatusTone(item.paymentStatus);

        return (
          <li key={item.subscriptionId} className={styles.upcomingItem}>
            <div className={styles.upcomingMain}>
              <div className={styles.upcomingTitleRow}>
                <Typography variant="subtitle1" component="p" className={styles.upcomingName}>
                  {item.name}
                </Typography>
                <span className={`${styles.badge} ${styles[`badge_${paymentTone}`]}`}>
                  {getDashboardPaymentStatusLabel(item.paymentStatus)}
                </span>
              </div>
              <Typography variant="body2" className={styles.upcomingMeta}>
                {item.categoryName || "기타"} · {formatDashboardDate(item.nextPaymentDate)}
              </Typography>
            </div>
            <Typography variant="subtitle2" component="p" className={styles.upcomingAmount}>
              {formatOriginalAndKrwAmount(item.price, item.currency || "KRW", item.convertedPriceKrw)}
            </Typography>
          </li>
        );
      })}
    </ul>
  );
}
