import { yupResolver } from "@hookform/resolvers/yup";
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from "@mui/material";
import dayjs from "dayjs";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import {
  BILLING_CYCLE_OPTIONS,
  SUBSCRIPTION_STATUS_OPTIONS,
} from "../../../features/subscription/utils/subscriptionLabel";
import { subscriptionSchema } from "../../../features/subscription/validation/subscriptionSchema";
import styles from "../SubscriptionListPage.module.css";

const DEFAULT_FORM_VALUES = {
  categoryId: "",
  name: "",
  price: "",
  currency: "KRW",
  billingCycle: "MONTHLY",
  nextPaymentDate: dayjs().format("YYYY-MM-DD"),
  paymentMethod: "",
  memo: "",
  status: "ACTIVE",
};

function toFormValues(subscription) {
  if (!subscription) {
    return DEFAULT_FORM_VALUES;
  }

  return {
    categoryId: subscription.categoryId || "",
    name: subscription.name || "",
    price: subscription.price ?? "",
    currency: subscription.currency || "KRW",
    billingCycle: subscription.billingCycle || "MONTHLY",
    nextPaymentDate: subscription.nextPaymentDate || dayjs().format("YYYY-MM-DD"),
    paymentMethod: subscription.paymentMethod || "",
    memo: subscription.memo || "",
    status: subscription.status || "ACTIVE",
  };
}

export function SubscriptionFormModal({
  open,
  mode,
  categories,
  initialValues,
  isInitialLoading,
  isSubmitting,
  errorMessage,
  onClose,
  onSubmit,
}) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(subscriptionSchema),
    defaultValues: DEFAULT_FORM_VALUES,
  });

  useEffect(() => {
    if (open) {
      reset(toFormValues(initialValues));
    }
  }, [initialValues, open, reset]);

  const submitForm = (values) => {
    onSubmit({
      categoryId: Number(values.categoryId),
      name: values.name.trim(),
      price: Number(values.price),
      currency: values.currency.trim().toUpperCase(),
      billingCycle: values.billingCycle,
      nextPaymentDate: values.nextPaymentDate,
      paymentMethod: values.paymentMethod.trim(),
      memo: values.memo?.trim() || null,
      status: values.status,
    });
  };

  const title = mode === "edit" ? "구독 수정" : "구독 추가";
  const disabled = isSubmitting || isInitialLoading;

  return (
    <Dialog
      open={open}
      onClose={disabled ? undefined : onClose}
      fullWidth
      maxWidth="sm"
      scroll="paper"
      PaperProps={{
        className: styles.dialogPaper,
        sx: {
          maxHeight: { xs: "calc(100dvh - 24px)", sm: "calc(100dvh - 32px)" },
          margin: { xs: "12px", sm: "32px" },
          overflow: "hidden",
          width: { xs: "calc(100% - 24px)", sm: "calc(100% - 64px)" },
        },
      }}
    >
      <DialogTitle>{title}</DialogTitle>
      <form onSubmit={handleSubmit(submitForm)} data-testid="subscription-form">
        <DialogContent className={styles.dialogContent}>
          <Stack spacing={2}>
            {errorMessage && <Alert severity="error">{errorMessage}</Alert>}
            {isInitialLoading && <Alert severity="info">구독 정보를 불러오는 중입니다.</Alert>}

            <TextField
              select
              label="카테고리"
              SelectProps={{ native: true }}
              InputLabelProps={{ shrink: true }}
              inputProps={{ "data-testid": "subscription-category-select" }}
              fullWidth
              disabled={disabled}
              error={Boolean(errors.categoryId)}
              helperText={errors.categoryId?.message}
              {...register("categoryId")}
            >
              <option value="">선택해주세요</option>
              {categories.map((category) => (
                <option key={category.categoryId} value={category.categoryId}>
                  {category.name}
                </option>
              ))}
            </TextField>

            <TextField
              label="구독 이름"
              fullWidth
              disabled={disabled}
              InputLabelProps={{ shrink: true }}
              inputProps={{ "data-testid": "subscription-name-input" }}
              error={Boolean(errors.name)}
              helperText={errors.name?.message}
              {...register("name")}
            />

            <div className={styles.formGrid}>
              <TextField
                label="금액"
                type="number"
                fullWidth
                disabled={disabled}
                InputLabelProps={{ shrink: true }}
                inputProps={{ min: 0, step: 100, "data-testid": "subscription-price-input" }}
                error={Boolean(errors.price)}
                helperText={errors.price?.message}
                {...register("price")}
              />

              <TextField
                label="통화"
                fullWidth
                disabled={disabled}
                InputLabelProps={{ shrink: true }}
                inputProps={{ maxLength: 3, "data-testid": "subscription-currency-input" }}
                error={Boolean(errors.currency)}
                helperText={errors.currency?.message}
                {...register("currency")}
              />
            </div>

            <div className={styles.formGrid}>
              <TextField
                select
                label="결제 주기"
                SelectProps={{ native: true }}
                InputLabelProps={{ shrink: true }}
                inputProps={{ "data-testid": "subscription-billing-cycle-select" }}
                fullWidth
                disabled={disabled}
                error={Boolean(errors.billingCycle)}
                helperText={errors.billingCycle?.message}
                {...register("billingCycle")}
              >
                {BILLING_CYCLE_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </TextField>

              <TextField
                label="다음 결제일"
                type="date"
                fullWidth
                disabled={disabled}
                InputLabelProps={{ shrink: true }}
                inputProps={{ "data-testid": "subscription-next-payment-date-input" }}
                error={Boolean(errors.nextPaymentDate)}
                helperText={errors.nextPaymentDate?.message}
                {...register("nextPaymentDate")}
              />
            </div>

            <div className={styles.formGrid}>
              <TextField
                label="결제 수단"
                fullWidth
                disabled={disabled}
                InputLabelProps={{ shrink: true }}
                inputProps={{ maxLength: 30, "data-testid": "subscription-payment-method-input" }}
                error={Boolean(errors.paymentMethod)}
                helperText={errors.paymentMethod?.message}
                {...register("paymentMethod")}
              />

              <TextField
                select
                label="구독 상태"
                SelectProps={{ native: true }}
                InputLabelProps={{ shrink: true }}
                inputProps={{ "data-testid": "subscription-status-select" }}
                fullWidth
                disabled={disabled}
                error={Boolean(errors.status)}
                helperText={errors.status?.message}
                {...register("status")}
              >
                {SUBSCRIPTION_STATUS_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </TextField>
            </div>

            <TextField
              label="메모"
              fullWidth
              multiline
              minRows={3}
              disabled={disabled}
              InputLabelProps={{ shrink: true }}
              inputProps={{ maxLength: 500, "data-testid": "subscription-memo-input" }}
              error={Boolean(errors.memo)}
              helperText={errors.memo?.message || "최대 500자"}
              {...register("memo")}
            />
          </Stack>
        </DialogContent>

        <DialogActions className={styles.dialogActions}>
          <Button type="button" onClick={onClose} disabled={disabled}>
            취소
          </Button>
          <Button type="submit" variant="contained" disabled={disabled} data-testid="subscription-submit-button">
            {isSubmitting ? "저장 중" : "저장"}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
