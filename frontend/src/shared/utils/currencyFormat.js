export const CURRENCY_OPTIONS = [
  { value: "KRW", label: "KRW ₩", symbol: "₩", integerOnly: true },
  { value: "USD", label: "USD $", symbol: "$", integerOnly: false },
  { value: "JPY", label: "JPY ¥", symbol: "¥", integerOnly: true },
  { value: "CNY", label: "CNY ¥", symbol: "¥", integerOnly: false },
  { value: "EUR", label: "EUR €", symbol: "€", integerOnly: false },
];

const currencyMap = Object.fromEntries(CURRENCY_OPTIONS.map((option) => [option.value, option]));

export function isSupportedCurrency(currency) {
  return Boolean(currencyMap[currency]);
}

export function isIntegerCurrency(currency) {
  return currencyMap[currency]?.integerOnly || false;
}

export function getCurrencyAmountStep(currency) {
  return isIntegerCurrency(currency) ? 1 : 0.01;
}

export function getCurrencyHelperText(currency) {
  return isIntegerCurrency(currency)
    ? "정수 금액을 입력하세요."
    : "소수 2자리까지 입력할 수 있습니다.";
}

export function formatCurrencyAmount(amount, currency = "KRW") {
  const numericAmount = Number(amount);

  if (Number.isNaN(numericAmount)) {
    return `${amount ?? "-"} ${currency}`;
  }

  if (currency === "KRW") {
    return `${Math.round(numericAmount).toLocaleString("ko-KR")}원`;
  }

  const option = currencyMap[currency] || { symbol: "", integerOnly: false };
  const fractionDigits = option.integerOnly ? 0 : 2;
  return `${option.symbol}${numericAmount.toLocaleString("ko-KR", {
    minimumFractionDigits: fractionDigits,
    maximumFractionDigits: fractionDigits,
  })}`;
}

export function formatKrwAmount(amount) {
  const numericAmount = Number(amount || 0);
  return `${Math.round(numericAmount).toLocaleString("ko-KR")}원`;
}

export function formatOriginalAndKrwAmount(price, currency = "KRW", convertedPriceKrw) {
  if (currency === "KRW") {
    return formatKrwAmount(price);
  }

  const originalAmount = formatCurrencyAmount(price, currency);
  if (convertedPriceKrw === null || convertedPriceKrw === undefined) {
    return `${originalAmount} · 환산 불가`;
  }

  return `${originalAmount} · 약 ${formatKrwAmount(convertedPriceKrw)}`;
}
