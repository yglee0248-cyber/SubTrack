import { yupResolver } from "@hookform/resolvers/yup";
import {
  Alert,
  Button,
  CircularProgress,
  Snackbar,
  TextField,
  Typography,
} from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import dayjs from "dayjs";
import { useEffect, useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { Link as RouterLink } from "react-router-dom";
import * as yup from "yup";
import {
  changeMyPassword,
  getMyProfile,
  updateMyProfile,
} from "../../features/member/api/memberApi";
import { useAuthStore } from "../../features/auth/store/authStore";
import { getApiErrorMessage } from "../../shared/utils/errorMessage";
import styles from "./MyPage.module.css";

const profileSchema = yup.object({
  nickname: yup
    .string()
    .trim()
    .required("닉네임을 입력해주세요.")
    .min(2, "닉네임은 2자 이상이어야 합니다.")
    .max(30, "닉네임은 30자 이하로 입력해주세요."),
});

const passwordSchema = yup.object({
  currentPassword: yup.string().required("현재 비밀번호를 입력해주세요."),
  newPassword: yup
    .string()
    .required("새 비밀번호를 입력해주세요.")
    .min(8, "새 비밀번호는 8자 이상이어야 합니다."),
  confirmPassword: yup
    .string()
    .required("새 비밀번호 확인을 입력해주세요.")
    .oneOf([yup.ref("newPassword")], "새 비밀번호와 확인 값이 일치하지 않습니다."),
});

function getInitial(nickname) {
  if (!nickname) {
    return "S";
  }

  return nickname.trim().charAt(0).toUpperCase();
}

function formatDate(value) {
  return value ? dayjs(value).format("YYYY.MM.DD") : "-";
}

function MyPage() {
  const queryClient = useQueryClient();
  const storedMember = useAuthStore((state) => state.member);
  const setMember = useAuthStore((state) => state.setMember);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success",
  });

  const profileQuery = useQuery({
    queryKey: ["member", "me"],
    queryFn: getMyProfile,
    staleTime: 0,
    refetchOnMount: "always",
  });

  const profile = profileQuery.data || storedMember || {};
  const initial = useMemo(() => getInitial(profile.nickname || profile.email), [profile.nickname, profile.email]);

  const profileForm = useForm({
    resolver: yupResolver(profileSchema),
    defaultValues: {
      nickname: profile.nickname || "",
    },
  });

  const passwordForm = useForm({
    resolver: yupResolver(passwordSchema),
    defaultValues: {
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    },
  });

  useEffect(() => {
    if (profileQuery.data) {
      setMember({
        memberId: profileQuery.data.memberId,
        email: profileQuery.data.email,
        nickname: profileQuery.data.nickname,
      });
      profileForm.reset({
        nickname: profileQuery.data.nickname || "",
      });
    }
  }, [profileQuery.data, profileForm, setMember]);

  const profileMutation = useMutation({
    mutationFn: updateMyProfile,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["member", "me"] });
      setMember({
        memberId: data.memberId,
        email: data.email,
        nickname: data.nickname,
      });
      showSnackbar("프로필이 저장되었습니다.", "success");
    },
    onError: (error) => {
      showSnackbar(getApiErrorMessage(error, "프로필 저장에 실패했습니다."), "error");
    },
  });

  const passwordMutation = useMutation({
    mutationFn: changeMyPassword,
    onSuccess: () => {
      passwordForm.reset();
      showSnackbar("비밀번호가 변경되었습니다.", "success");
    },
    onError: (error) => {
      showSnackbar(getApiErrorMessage(error, "비밀번호 변경에 실패했습니다."), "error");
    },
  });

  function showSnackbar(message, severity = "success") {
    setSnackbar({
      open: true,
      message,
      severity,
    });
  }

  function submitProfile(values) {
    profileMutation.mutate({
      nickname: values.nickname.trim(),
    });
  }

  function submitPassword(values) {
    passwordMutation.mutate(values);
  }

  return (
    <section className={styles.page}>
      <div className={styles.heroCard}>
        <div className={styles.avatar} aria-hidden="true">
          {initial}
        </div>
        <div className={styles.heroText}>
          <Typography variant="h4" component="h1" className={styles.title}>
            {profile.nickname || "사용자"}님 계정
          </Typography>
          <Typography variant="body2" className={styles.description}>
            로그인 정보와 SubTrack의 표시 기준을 관리합니다.
          </Typography>
          <div className={styles.heroMeta}>
            <span>{profile.email || "-"}</span>
            <span>가입일 {formatDate(profile.createdAt)}</span>
          </div>
        </div>
      </div>

      {profileQuery.isLoading && (
        <div className={styles.loadingBox}>
          <CircularProgress size={28} />
          <Typography variant="body2">내 정보를 불러오는 중입니다.</Typography>
        </div>
      )}

      {profileQuery.isError && (
        <Alert severity="warning" className={styles.stateBox}>
          {getApiErrorMessage(profileQuery.error, "내 정보를 다시 불러오지 못했습니다.")}
        </Alert>
      )}

      <div className={styles.grid}>
        <section className={styles.card}>
          <div className={styles.cardHeader}>
            <Typography variant="h6" component="h2" className={styles.cardTitle}>
              계정 정보
            </Typography>
            <span className={styles.badge}>일반 로그인</span>
          </div>
          <dl className={styles.infoList}>
            <div>
              <dt>이메일</dt>
              <dd>{profile.email || "-"}</dd>
            </div>
            <div>
              <dt>닉네임</dt>
              <dd>{profile.nickname || "-"}</dd>
            </div>
            <div>
              <dt>가입일</dt>
              <dd>{formatDate(profile.createdAt)}</dd>
            </div>
          </dl>
          <p className={styles.helpText}>
            이메일은 로그인 식별자로 사용됩니다.
          </p>
        </section>

        <section className={styles.card}>
          <Typography variant="h6" component="h2" className={styles.cardTitle}>
            프로필 수정
          </Typography>
          <form className={styles.form} onSubmit={profileForm.handleSubmit(submitProfile)}>
            <TextField
              label="닉네임"
              fullWidth
              InputLabelProps={{ shrink: true }}
              error={Boolean(profileForm.formState.errors.nickname)}
              helperText={profileForm.formState.errors.nickname?.message || "2자 이상 30자 이하로 입력해주세요."}
              {...profileForm.register("nickname")}
            />
            <div className={styles.buttonRow}>
              <Button
                type="button"
                variant="text"
                onClick={() => profileForm.reset({ nickname: profile.nickname || "" })}
                disabled={profileMutation.isPending}
              >
                취소
              </Button>
              <Button type="submit" variant="contained" disabled={profileMutation.isPending}>
                {profileMutation.isPending ? "저장 중" : "저장"}
              </Button>
            </div>
          </form>
        </section>

        <section className={styles.card}>
          <Typography variant="h6" component="h2" className={styles.cardTitle}>
            비밀번호 변경
          </Typography>
          <form className={styles.form} onSubmit={passwordForm.handleSubmit(submitPassword)}>
            <TextField
              label="현재 비밀번호"
              type="password"
              fullWidth
              autoComplete="current-password"
              InputLabelProps={{ shrink: true }}
              error={Boolean(passwordForm.formState.errors.currentPassword)}
              helperText={passwordForm.formState.errors.currentPassword?.message}
              {...passwordForm.register("currentPassword")}
            />
            <TextField
              label="새 비밀번호"
              type="password"
              fullWidth
              autoComplete="new-password"
              InputLabelProps={{ shrink: true }}
              error={Boolean(passwordForm.formState.errors.newPassword)}
              helperText={passwordForm.formState.errors.newPassword?.message || "8자 이상 입력해주세요."}
              {...passwordForm.register("newPassword")}
            />
            <TextField
              label="새 비밀번호 확인"
              type="password"
              fullWidth
              autoComplete="new-password"
              InputLabelProps={{ shrink: true }}
              error={Boolean(passwordForm.formState.errors.confirmPassword)}
              helperText={passwordForm.formState.errors.confirmPassword?.message}
              {...passwordForm.register("confirmPassword")}
            />
            <div className={styles.buttonRow}>
              <Button
                type="button"
                variant="text"
                onClick={() => passwordForm.reset()}
                disabled={passwordMutation.isPending}
              >
                입력 초기화
              </Button>
              <Button type="submit" variant="contained" disabled={passwordMutation.isPending}>
                {passwordMutation.isPending ? "변경 중" : "비밀번호 변경"}
              </Button>
            </div>
          </form>
        </section>

        <section className={styles.card}>
          <Typography variant="h6" component="h2" className={styles.cardTitle}>
            환율 / 표시 기준
          </Typography>
          <ul className={styles.policyList}>
            <li>
              <strong>기준 통화</strong>
              <span>KRW</span>
            </li>
            <li>
              <strong>외화 환산</strong>
              <span>Frankfurter 캐시 환율 기준</span>
            </li>
            <li>
              <strong>실제 청구액</strong>
              <span>카드사 환율, 수수료, 매입 시점에 따라 차이 가능</span>
            </li>
          </ul>
        </section>

        <section className={styles.card}>
          <Typography variant="h6" component="h2" className={styles.cardTitle}>
            SubTrack 사용 기준
          </Typography>
          <p className={styles.paragraph}>
            SubTrack은 등록된 구독 정보를 기준으로 자동결제 예정 흐름과 예상 지출을 보여주는 서비스입니다.
          </p>
          <ul className={styles.policyList}>
            <li>실제 결제 완료 여부보다 다음 결제일과 월별 예상 지출 관리에 집중합니다.</li>
            <li>일시정지/해지 상태 이력을 기준으로 선택 월 예상 지출을 계산합니다.</li>
            <li>외화 구독은 캐시 환율을 기준으로 원화 예상 금액을 함께 보여줍니다.</li>
          </ul>
        </section>

        <section className={styles.card}>
          <Typography variant="h6" component="h2" className={styles.cardTitle}>
            빠른 이동
          </Typography>
          <div className={styles.quickLinks}>
            <Button component={RouterLink} to="/dashboard" variant="outlined">
              Dashboard 보기
            </Button>
            <Button component={RouterLink} to="/subscriptions" variant="outlined">
              구독 목록 보기
            </Button>
            <Button component={RouterLink} to="/subscriptions" variant="contained">
              구독 추가하기
            </Button>
          </div>
        </section>
      </div>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={2800}
        onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
        anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
      >
        <Alert
          severity={snackbar.severity}
          variant="filled"
          onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </section>
  );
}

export default MyPage;
