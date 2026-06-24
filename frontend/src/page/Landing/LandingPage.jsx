import { Button, Stack, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../../features/auth/store/authStore";
import styles from "./LandingPage.module.css";

function LandingPage() {
  const navigate = useNavigate();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return (
    <section className={styles.page}>
      <div className={styles.content}>
        <Typography variant="h3" component="h1" className={styles.title}>
          SubTrack
        </Typography>
        <Typography variant="body1" className={styles.description}>
          반복 결제와 구독 정보를 한 곳에서 관리하는 개인 구독 관리 서비스입니다.
        </Typography>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={1.5}>
          <Button variant="contained" size="large" onClick={() => navigate(isAuthenticated ? "/dashboard" : "/login")}>
            시작하기
          </Button>
          {!isAuthenticated && (
            <Button variant="outlined" size="large" onClick={() => navigate("/signup")}>
              회원가입
            </Button>
          )}
        </Stack>
      </div>
    </section>
  );
}

export default LandingPage;
