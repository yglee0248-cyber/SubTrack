import dayjs from "dayjs";
import * as yup from "yup";

function emptyStringToUndefined(value, originalValue) {
  return originalValue === "" ? undefined : value;
}

function isDateBefore(left, right) {
  return Boolean(left && right && left < right);
}

function isInactiveStatus(status) {
  return status === "PAUSED" || status === "CANCELED";
}

export const subscriptionSchema = yup.object({
  categoryId: yup
    .number()
    .transform(emptyStringToUndefined)
    .typeError("카테고리를 선택해주세요.")
    .required("카테고리를 선택해주세요."),
  name: yup
    .string()
    .trim()
    .required("구독 이름을 입력해주세요.")
    .max(100, "구독 이름은 100자 이하로 입력해주세요."),
  price: yup
    .number()
    .transform(emptyStringToUndefined)
    .typeError("금액은 숫자로 입력해주세요.")
    .required("금액을 입력해주세요.")
    .min(0, "금액은 0 이상이어야 합니다."),
  currency: yup
    .string()
    .trim()
    .uppercase()
    .required("통화 코드를 입력해주세요.")
    .length(3, "통화 코드는 3자로 입력해주세요."),
  billingCycle: yup
    .string()
    .oneOf(["MONTHLY", "YEARLY"], "결제 주기를 선택해주세요.")
    .required("결제 주기를 선택해주세요."),
  billingStartDate: yup.string().required("구독 시작일 또는 첫 결제일을 선택해주세요."),
  paymentMethod: yup
    .string()
    .trim()
    .required("결제 수단을 입력해주세요.")
    .max(30, "결제 수단은 30자 이하로 입력해주세요."),
  memo: yup.string().trim().max(500, "메모는 500자 이하로 입력해주세요."),
  status: yup
    .string()
    .oneOf(["ACTIVE", "PAUSED", "CANCELED"], "구독 상태를 선택해주세요.")
    .required("구독 상태를 선택해주세요."),
  statusEffectiveDate: yup
    .string()
    .nullable()
    .when("status", {
      is: isInactiveStatus,
      then: (schema) =>
        schema
          .required("상태 적용일을 선택해주세요.")
          .test("not-before-billing-start", "상태 적용일은 구독 시작일보다 이전일 수 없습니다.", function (value) {
            return !isDateBefore(value, this.parent.billingStartDate);
          })
          .test("not-future", "미래 날짜의 상태 변경 예약은 아직 지원하지 않습니다.", (value) => {
            return value <= dayjs().format("YYYY-MM-DD");
          }),
      otherwise: (schema) => schema.notRequired(),
    }),
});
