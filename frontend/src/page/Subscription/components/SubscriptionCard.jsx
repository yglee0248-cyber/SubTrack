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
  const isActive = subscription.status === "ACTIVE";
  const statusDateLabel = subscription.status === "CANCELED" ? "해지일" : "일시정지 시작일";
  const statusDateNotice = subscription.status === "CANCELED"
    ? "해지 이전 기간의 통계는 상태 이력 기준으로 유지됩니다."
    : "일시정지 이전 기간의 통계는 상태 이력 기준으로 유지됩니다.";

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
          {isActive && (
            <span className={`${styles.badge} ${styles[`badge_${paymentTone}`]}`}>
              {getPaymentStatusLabel(subscription.paymentStatus)}
            </span>
          )}
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
          <dt>{isActive ? "다음 결제일" : statusDateLabel}</dt>
          <dd>{formatPaymentDate(isActive ? subscription.nextPaymentDate : subscription.statusEffectiveDate)}</dd>
        </div>
        <div>
          <dt>결제 수단</dt>
          <dd>{subscription.paymentMethod || "-"}</dd>
        </div>
      </dl>

      {!isActive && <p className={styles.statusNotice}>{statusDateNotice}</p>}

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
