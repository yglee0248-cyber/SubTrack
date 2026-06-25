import { Button, Typography } from "@mui/material";
import styles from "../SubscriptionListPage.module.css";

export function SubscriptionEmptyState({ onCreate }) {
  return (
    <div className={styles.emptyState}>
      <Typography variant="h6" component="p">
        아직 등록된 구독이 없습니다.
      </Typography>
      <Typography variant="body2">
        매달 빠져나가는 서비스를 등록하면 결제일과 상태를 한눈에 볼 수 있어요.
      </Typography>
      <Button variant="contained" onClick={onCreate}>
        첫 구독 등록
      </Button>
    </div>
  );
}
