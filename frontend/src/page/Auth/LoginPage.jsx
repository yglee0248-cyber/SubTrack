import { yupResolver } from "@hookform/resolvers/yup";
import { Alert, Button, Link, Stack, TextField, Typography } from "@mui/material";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link as RouterLink, useLocation, useNavigate } from "react-router-dom";
import { login } from "../../features/auth/api/authApi";
import { useAuthStore } from "../../features/auth/store/authStore";
import { loginSchema } from "../../features/auth/validation/authSchema";
import { getApiErrorMessage } from "../../shared/utils/errorMessage";
import styles from "./AuthPage.module.css";

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const setLogin = useAuthStore((state) => state.setLogin);
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(loginSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const onSubmit = async (values) => {
    setErrorMessage("");
    setIsSubmitting(true);

    try {
      const data = await login(values);
      setLogin(data.accessToken, data.member);
      navigate(location.state?.from?.pathname || "/dashboard", { replace: true });
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, "로그인에 실패했습니다."));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className={styles.page}>
      <div className={styles.authShell}>
        <aside className={styles.introPanel}>
          <span className={styles.badge}>개인 구독 관리</span>

          <h1 className={styles.introTitle}>
            반복 결제 관리를 시작하세요
          </h1>

          <Typography variant="body1" className={styles.introDescription}>
            등록한 구독의 다음 결제일과 월별 예상 지출을 한 곳에서 확인합니다.
          </Typography>

          <div className={styles.featureList} aria-label="SubTrack 핵심 기능">
            <span>월별 예상 구독료</span>
            <span>외화 KRW 환산</span>
            <span>결제 예정 관리</span>
          </div>
        </aside>

        <div className={styles.panel}>
          <Typography variant="h4" component="h2" className={styles.title}>
            로그인
          </Typography>
          <Typography variant="body2" className={styles.subtitle}>
            SubTrack 계정으로 구독 관리를 이어가세요.
          </Typography>

          <form className={`${styles.form} ${styles.authForm}`} onSubmit={handleSubmit(onSubmit)}>
            <Stack spacing={1.5}>
              {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

              <TextField
                label="이메일"
                type="email"
                autoComplete="email"
                fullWidth
                InputLabelProps={{ shrink: true }}
                error={Boolean(errors.email)}
                helperText={errors.email?.message}
                {...register("email")}
              />

              <TextField
                label="비밀번호"
                type="password"
                autoComplete="current-password"
                fullWidth
                InputLabelProps={{ shrink: true }}
                error={Boolean(errors.password)}
                helperText={errors.password?.message}
                {...register("password")}
              />

              <Button type="submit" variant="contained" size="large" disabled={isSubmitting} fullWidth>
                {isSubmitting ? "로그인 중" : "로그인"}
              </Button>
            </Stack>
          </form>

          <Typography variant="body2" className={styles.helperText}>
            계정이 없나요?{" "}
            <Link component={RouterLink} to="/signup">
              회원가입
            </Link>
          </Typography>
        </div>
      </div>
    </section>
  );
}

export default LoginPage;