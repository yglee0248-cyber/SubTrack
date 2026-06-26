import { Button } from "@mui/material";
import {
  formatPaymentDate,
  formatSubscriptionPrice,
  getBillingCycleLabel,
  getPaymentStatusLabel,
  getPaymentStatusTone,
  getStatusTone,
  getSubscriptionStatusLabel,
} from "../../../features/subscription/utils/subscriptionLabel";
import styles from "../SubscriptionListPage.module.css";

export function SubscriptionCard({ subscription, category, onEdit, onDelete, isDeleting }) {
  const statusTone = getStatusTone(subscription.status);
  const paymentTone = getPaymentStatusTone(subscription.paymentStatus);
  const categoryColor = category?.colorCode || "#98a2b3";

  return (
    <article className={styles.card} data-testid="subscription-card">
      <div className={styles.cardHeader}>
        <div className={styles.cardTitleGroup}>
          <span className={styles.categoryLine}>
            <span className={styles.categoryDot} style={{ backgroundColor: categoryColor }} aria-hidden="true" />
            {subscription.categoryName || category?.name || "기타"}
          </span>
          <h2 className={styles.cardTitle}>{subscription.name}</h2>
        </div>
        <div className={styles.badgeGroup}>
          <span className={`${styles.badge} ${styles[`badge_${paymentTone}`]}`}>
            {getPaymentStatusLabel(subscription.paymentStatus)}
          </span>
          <span className={`${styles.badge} ${styles[`badge_${statusTone}`]}`}>
            {getSubscriptionStatusLabel(subscription.status)}
          </span>
        </div>
      </div>

      <dl className={styles.cardMeta}>
        <div>
          <dt>금액</dt>
          <dd>{formatSubscriptionPrice(subscription.price, subscription.currency)}</dd>
        </div>
        <div>
          <dt>주기</dt>
          <dd>{getBillingCycleLabel(subscription.billingCycle)}</dd>
        </div>
        <div>
          <dt>구독 시작일</dt>
          <dd>{formatPaymentDate(subscription.billingStartDate || subscription.nextPaymentDate)}</dd>
        </div>
        <div>
          <dt>다음 결제일</dt>
          <dd>{formatPaymentDate(subscription.nextPaymentDate)}</dd>
        </div>
        <div>
          <dt>결제 수단</dt>
          <dd>{subscription.paymentMethod || "-"}</dd>
        </div>
      </dl>

      <div className={styles.cardActions}>
        <Button variant="outlined" size="small" onClick={() => onEdit(subscription)}>
          수정
        </Button>
        <Button
          variant="text"
          color="error"
          size="small"
          onClick={() => onDelete(subscription)}
          disabled={isDeleting}
        >
          삭제
        </Button>
      </div>
    </article>
  );
}
