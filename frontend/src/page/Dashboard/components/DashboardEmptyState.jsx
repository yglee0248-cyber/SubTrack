import { Typography } from "@mui/material";
import styles from "../DashboardPage.module.css";

export function DashboardEmptyState({ title, description }) {
  return (
    <div className={styles.emptyState}>
      <Typography variant="subtitle1" component="p" className={styles.emptyTitle}>
        {title}
      </Typography>
      {description && (
        <Typography variant="body2" className={styles.emptyDescription}>
          {description}
        </Typography>
      )}
    </div>
  );
}
