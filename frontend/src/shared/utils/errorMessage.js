export function getApiErrorMessage(error, fallbackMessage) {
  return (
    error?.response?.data?.message ||
    error?.response?.data?.error?.details?.[0]?.reason ||
    fallbackMessage ||
    "요청 처리 중 오류가 발생했습니다."
  );
}
