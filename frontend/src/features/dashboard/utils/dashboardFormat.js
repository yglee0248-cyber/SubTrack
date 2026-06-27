import dayjs from "dayjs";
import { formatKrwAmount } from "../../../shared/utils/currencyFormat";

const paymentStatusLabelMap = {
  UPCOMING: "예정",
  DUE_SOON: "임박",
  DUE_TODAY: "오늘 결제",
};

export function getCurrentYearMonth() {
  return dayjs().format("YYYY-MM");
}

export function formatYearMonthLabel(yearMonth) {
  if (!yearMonth) {
    return "-";
  }

  const parsed = dayjs(`${yearMonth}-01`);
  return parsed.isValid() ? parsed.format("YYYY년 M월") : yearMonth;
}

export function formatDashboardAmount(amount) {
  return formatKrwAmount(amount);
}

export function formatDashboardCount(count, unit = "개") {
  const numericCount = Number(count || 0);
  return `${numericCount.toLocaleString("ko-KR")}${unit}`;
}

export function formatDashboardDate(date) {
  if (!date) {
    return "-";
  }

  return dayjs(date).format("YYYY.MM.DD");
}

export function getDashboardPaymentStatusLabel(paymentStatus) {
  return paymentStatusLabelMap[paymentStatus] || paymentStatus || "-";
}

export function getDashboardPaymentStatusTone(paymentStatus) {
  if (paymentStatus === "DUE_TODAY") {
    return "today";
  }

  if (paymentStatus === "DUE_SOON") {
    return "warning";
  }

  return "normal";
}
