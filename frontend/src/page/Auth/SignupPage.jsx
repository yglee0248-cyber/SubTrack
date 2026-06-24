import { yupResolver } from "@hookform/resolvers/yup";
import { Alert, Button, Link, Stack, TextField, Typography } from "@mui/material";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { signup } from "../../features/auth/api/authApi";
import { signupSchema } from "../../features/auth/validation/authSchema";
import { getApiErrorMessage } from "../../shared/utils/errorMessage";
import styles from "./AuthPage.module.css";

function SignupPage() {
  const navigate = useNavigate();
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(signupSchema),
    defaultValues: {
      email: "",
      password: "",
      nickname: "",
    },
  });

  const onSubmit = async (values) => {
    setErrorMessage("");
    setIsSubmitting(true);

    try {
      await signup(values);
      navigate("/login");
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, "회원가입에 실패했습니다."));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className={styles.page}>
      <div className={styles.panel}>
        <Typography variant="h4" component="h1" className={styles.title}>
          회원가입
        </Typography>
        <Typography variant="body2" className={styles.subtitle}>
          이메일과 닉네임으로 SubTrack 계정을 만듭니다.
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
              label="닉네임"
              autoComplete="nickname"
              fullWidth
              error={Boolean(errors.nickname)}
              helperText={errors.nickname?.message}
              {...register("nickname")}
            />
            <TextField
              label="비밀번호"
              type="password"
              autoComplete="new-password"
              fullWidth
              error={Boolean(errors.password)}
              helperText={errors.password?.message}
              {...register("password")}
            />
            <Button type="submit" variant="contained" size="large" disabled={isSubmitting}>
              {isSubmitting ? "가입 중" : "회원가입"}
            </Button>
          </Stack>
        </form>

        <Typography variant="body2" className={styles.helperText}>
          이미 계정이 있나요?{" "}
          <Link component={RouterLink} to="/login">
            로그인
          </Link>
        </Typography>
      </div>
    </section>
  );
}

export default SignupPage;
