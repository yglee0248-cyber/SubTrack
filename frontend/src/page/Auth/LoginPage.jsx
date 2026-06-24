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
      <div className={styles.panel}>
        <Typography variant="h4" component="h1" className={styles.title}>
          로그인
        </Typography>
        <Typography variant="body2" className={styles.subtitle}>
          SubTrack 계정으로 구독 관리를 시작하세요.
        </Typography>

        <form className={styles.form} onSubmit={handleSubmit(onSubmit)}>
          <Stack spacing={2}>
            {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

            <TextField
              label="이메일"
              type="email"
              autoComplete="email"
              fullWidth
              error={Boolean(errors.email)}
              helperText={errors.email?.message}
              {...register("email")}
            />
            <TextField
              label="비밀번호"
              type="password"
              autoComplete="current-password"
              fullWidth
              error={Boolean(errors.password)}
              helperText={errors.password?.message}
              {...register("password")}
            />
            <Button type="submit" variant="contained" size="large" disabled={isSubmitting}>
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
    </section>
  );
}

export default LoginPage;
