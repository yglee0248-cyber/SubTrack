import { Button, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../../features/auth/store/authStore";
import styles from "./LandingPage.module.css";

const values = [
  {
    title: "월별 예상 지출 관리",
    description: "구독 시작일과 결제 주기를 기준으로 선택 월의 예상 구독료를 정리합니다.",
  },
  {
    title: "외화 구독 KRW 환산",
    description: "USD, JPY, CNY, EUR 구독도 캐시 환율로 원화 기준 지출 흐름을 확인합니다.",
  },
  {
    title: "결제 예정 흐름 확인",
    description: "오늘 기준 가까운 결제와 선택 월 전체 결제 예정 목록을 구분해서 보여줍니다.",
  },
];

function LandingPage() {
  const navigate = useNavigate();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return (
    <section className={styles.page}>
      <div className={styles.hero}>
        <div className={styles.heroCopy}>
          <span className={styles.badge}>개인 구독 관리 서비스</span>
          <Typography variant="h3" component="h1" className={styles.title}>
            반복 결제와 구독 지출을
            <br />
            한눈에 관리하세요
          </Typography>
          <Typography variant="body1" className={styles.description}>
            다음 결제일, 월별 예상 지출, 외화 구독의 원화 환산까지 한 곳에서 확인합니다.
          </Typography>
          <div className={styles.ctaRow}>
            <Button
              variant="contained"
              size="large"
              onClick={() => navigate(isAuthenticated ? "/dashboard" : "/signup")}
            >
              {isAuthenticated ? "Dashboard 보기" : "시작하기"}
            </Button>
            <Button
              variant="outlined"
              size="large"
              onClick={() => navigate(isAuthenticated ? "/subscriptions" : "/login")}
            >
              {isAuthenticated ? "구독 관리" : "로그인"}
            </Button>
          </div>
        </div>

        <aside className={styles.previewCard} aria-label="SubTrack dashboard preview">
          <div className={styles.previewHeader}>
            <span>예시 화면</span>
            <strong>KRW 환산 기준</strong>
          </div>
          <div className={styles.previewAmount}>
            <span>월간 예상 구독료</span>
            <strong>약 84,000원</strong>
          </div>
          <div className={styles.previewList}>
            <div>
              <span className={styles.dotBlue} />
              <p>
                <strong>Netflix</strong>
                <small>매월 · 17,000원</small>
              </p>
            </div>
            <div>
              <span className={styles.dotGreen} />
              <p>
                <strong>ChatGPT Plus</strong>
                <small>$22.00 · 약 33,700원</small>
              </p>
            </div>
            <div>
              <span className={styles.dotOrange} />
              <p>
                <strong>Japanese Service</strong>
                <small>¥1,200 · 약 11,400원</small>
              </p>
            </div>
          </div>
        </aside>
      </div>

      <div className={styles.valueGrid}>
        {values.map((value) => (
          <article key={value.title} className={styles.valueCard}>
            <Typography variant="h6" component="h2" className={styles.valueTitle}>
              {value.title}
            </Typography>
            <Typography variant="body2" className={styles.valueDescription}>
              {value.description}
            </Typography>
          </article>
        ))}
      </div>
    </section>
  );
}

export default LandingPage;
