import { CircularProgress, Typography } from "@mui/material";
import styles from "../DashboardPage.module.css";

export function SummaryCard({ label, value, helper, tone = "blue", loading }) {
  return (
    <article className={`${styles.summaryCard} ${styles[`summary_${tone}`]}`} aria-busy={loading}>
      <Typography variant="body2" component="p" className={styles.summaryLabel}>
        {label}
      </Typography>
      <div className={styles.summaryValueRow}>
        {loading ? (
          <CircularProgress size={22} />
        ) : (
          <Typography variant="h5" component="strong" className={styles.summaryValue}>
            {value}
          </Typography>
        )}
      </div>
      <Typography variant="caption" component="p" className={styles.summaryHelper}>
        {helper}
      </Typography>
    </article>
  );
}
