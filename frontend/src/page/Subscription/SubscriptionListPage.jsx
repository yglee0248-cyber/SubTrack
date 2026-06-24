import { Button, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import styles from "./SubscriptionListPage.module.css";

function SubscriptionListPage() {
  const navigate = useNavigate();

  return (
    <section className={styles.page}>
      <Typography variant="h4" component="h1" className={styles.title}>
        구독 목록
      </Typography>
      <Typography variant="body1" className={styles.description}>
        구독 CRUD 화면은 다음 작업에서 구현합니다.
      </Typography>
      <Button variant="outlined" onClick={() => navigate("/dashboard")}>
        대시보드로 돌아가기
      </Button>
    </section>
  );
}

export default SubscriptionListPage;
