import { Alert, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import dayjs from "dayjs";
import { getMe } from "../../features/auth/api/authApi";
import { useAuthStore } from "../../features/auth/store/authStore";
import styles from "./MyPage.module.css";

function MyPage() {
  const member = useAuthStore((state) => state.member);
  const { data, isError } = useQuery({
    queryKey: ["member", "me"],
    queryFn: getMe,
  });

  const profile = data || member;

  return (
    <section className={styles.page}>
      <Typography variant="h4" component="h1" className={styles.title}>
        My Page
      </Typography>
      {isError && <Alert severity="warning">내 정보를 다시 불러오지 못했습니다.</Alert>}
      <dl className={styles.profileList}>
        <div>
          <dt>이메일</dt>
          <dd>{profile?.email || "-"}</dd>
        </div>
        <div>
          <dt>닉네임</dt>
          <dd>{profile?.nickname || "-"}</dd>
        </div>
        <div>
          <dt>가입일</dt>
          <dd>{profile?.createdAt ? dayjs(profile.createdAt).format("YYYY-MM-DD") : "-"}</dd>
        </div>
      </dl>
    </section>
  );
}

export default MyPage;
