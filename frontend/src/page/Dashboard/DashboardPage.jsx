import { Alert, Button, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { getMe } from "../../features/auth/api/authApi";
import { useAuthStore } from "../../features/auth/store/authStore";
import styles from "./DashboardPage.module.css";

function DashboardPage() {
  const navigate = useNavigate();
  const member = useAuthStore((state) => state.member);
  const setMember = useAuthStore((state) => state.setMember);

  const { data, isError } = useQuery({
    queryKey: ["member", "me"],
    queryFn: getMe,
  });

  useEffect(() => {
    if (data) {
      setMember({
        memberId: data.memberId,
        email: data.email,
        nickname: data.nickname,
      });
    }
  }, [data, setMember]);

  const nickname = data?.nickname || member?.nickname || "사용자";

  return (
    <section className={styles.page}>
      <Typography variant="h4" component="h1" className={styles.title}>
        안녕하세요, {nickname}님
      </Typography>
      <Typography variant="body1" className={styles.description}>
        대시보드 요약과 차트는 다음 작업에서 연결할 예정입니다.
      </Typography>
      {isError && <Alert severity="warning">내 정보를 불러오지 못했습니다. 저장된 로그인 정보로 표시합니다.</Alert>}
      <Button variant="contained" onClick={() => navigate("/subscriptions")}>
        구독 목록으로 이동
      </Button>
    </section>
  );
}

export default DashboardPage;
