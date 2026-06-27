import dayjs from "dayjs";
import { formatCurrencyAmount } from "../../../shared/utils/currencyFormat";

export const BILLING_CYCLE_OPTIONS = [
  { value: "MONTHLY", label: "매월" },
  { value: "YEARLY", label: "매년" },
];

export const SUBSCRIPTION_STATUS_OPTIONS = [
  { value: "ACTIVE", label: "활성" },
  { value: "PAUSED", label: "일시정지" },
  { value: "CANCELED", label: "해지" },
];

export const PAYMENT_STATUS_OPTIONS = [
  { value: "UPCOMING", label: "예정" },
  { value: "DUE_SOON", label: "임박" },
  { value: "DUE_TODAY", label: "오늘 결제" },
];

const billingCycleLabelMap = Object.fromEntries(
  BILLING_CYCLE_OPTIONS.map((option) => [option.value, option.label])
);

const statusLabelMap = Object.fromEntries(
  SUBSCRIPTION_STATUS_OPTIONS.map((option) => [option.value, option.label])
);

const paymentStatusLabelMap = Object.fromEntries(
  PAYMENT_STATUS_OPTIONS.map((option) => [option.value, option.label])
);

export function getBillingCycleLabel(value) {
  return billingCycleLabelMap[value] || value || "-";
}

export function getSubscriptionStatusLabel(value) {
  return statusLabelMap[value] || value || "-";
}

export function getPaymentStatusLabel(value) {
  return paymentStatusLabelMap[value] || value || "-";
}

export function formatSubscriptionPrice(price, currency = "KRW") {
  return formatCurrencyAmount(price, currency);
}

export function formatPaymentDate(date) {
  if (!date) {
    return "-";
  }

  return dayjs(date).format("YYYY.MM.DD");
}

export function getStatusTone(status) {
  if (status === "ACTIVE") {
    return "active";
  }

  if (status === "PAUSED") {
    return "paused";
  }

  return "canceled";
}

export function getPaymentStatusTone(paymentStatus) {
  if (paymentStatus === "DUE_TODAY") {
    return "today";
  }

  if (paymentStatus === "DUE_SOON") {
    return "warning";
  }

  return "normal";
}
