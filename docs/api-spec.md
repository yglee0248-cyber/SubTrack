# SubTrack API Spec

## 1. 공통 응답 형식

### 성공 응답

```json
{
  "success": true,
  "message": "요청이 성공했습니다.",
  "data": {}
}
```

### 실패 응답

```json
{
  "success": false,
  "message": "요청 처리 중 오류가 발생했습니다.",
  "error": {
    "code": "VALIDATION_ERROR",
    "details": [
      {
        "field": "email",
        "reason": "이메일 형식이 올바르지 않습니다."
      }
    ]
  }
}
```

---

## 2. 공통 에러 코드

| HTTP | code | 설명 |
|---|---|---|
| 400 | VALIDATION_ERROR | 입력값 검증 실패 |
| 401 | UNAUTHORIZED | 인증 실패 |
| 403 | FORBIDDEN | 권한 없음 |
| 404 | NOT_FOUND | 리소스 없음 |
| 409 | DUPLICATE_RESOURCE | 중복 데이터 |
| 500 | INTERNAL_SERVER_ERROR | 서버 내부 오류 |

---

## 3. 인증 / 회원 API

## POST /api/auth/signup

회원가입 API입니다.

범위: P0

### Request

```json
{
  "email": "user@example.com",
  "password": "password123!",
  "nickname": "영민"
}
```

### Response data

```json
{
  "memberId": 1,
  "email": "user@example.com",
  "nickname": "영민"
}
```

### 예외

| 상황 | HTTP |
|---|---|
| 이메일 형식 오류 | 400 |
| 비밀번호 정책 불만족 | 400 |
| 이메일 중복 | 409 |

---

## POST /api/auth/login

로그인 API입니다.

범위: P0

### Request

```json
{
  "email": "user@example.com",
  "password": "password123!"
}
```

### Response data

```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer",
  "member": {
    "memberId": 1,
    "email": "user@example.com",
    "nickname": "영민"
  }
}
```

MVP에서는 refresh token을 구현하지 않습니다.

---

## POST /api/auth/logout

로그아웃 API입니다.

범위: P0

MVP에서는 서버에서 토큰 블랙리스트를 관리하지 않습니다.  
프론트에서 accessToken을 삭제하는 방식으로 처리합니다.

### Response data

```json
{
  "loggedOut": true
}
```

---

## GET /api/members/me

내 정보 조회 API입니다.

범위: P0

### Response data

```json
{
  "memberId": 1,
  "email": "user@example.com",
  "nickname": "영민",
  "pushEnabled": false,
  "createdAt": "2026-06-15T10:00:00"
}
```

---

## PATCH /api/members/me/nickname

닉네임 수정 API입니다.

범위: P0

### Request

```json
{
  "nickname": "youngmin"
}
```

### Response data

```json
{
  "memberId": 1,
  "nickname": "youngmin"
}
```

---

## PATCH /api/members/me/push-setting

푸시 설정 변경 API입니다.

범위: P1

### Request

```json
{
  "pushEnabled": true,
  "onesignalPlayerId": "onesignal-player-id"
}
```

### Response data

```json
{
  "pushEnabled": true,
  "onesignalPlayerId": "onesignal-player-id"
}
```

---

## 4. 카테고리 API

## GET /api/categories

기본 카테고리 목록 조회 API입니다.

범위: P0

### Response data

```json
[
  {
    "categoryId": 1,
    "name": "영상",
    "colorCode": "#E53935",
    "icon": "movie"
  }
]
```

---

## 5. 구독 API

## GET /api/subscriptions

구독 목록 조회, 검색, 필터 API입니다.

범위: P0

### Query

```txt
keyword=netflix
categoryId=1
status=ACTIVE
paymentStatus=DUE_SOON
page=0
size=20
```

### Response data

```json
{
  "content": [
    {
      "subscriptionId": 1,
      "name": "Netflix",
      "categoryName": "영상",
      "price": 17000,
      "currency": "KRW",
      "billingCycle": "MONTHLY",
      "billingAnchorDay": 25,
      "billingStartDate": "2025-12-25",
      "nextPaymentDate": "2026-06-25",
      "statusEffectiveDate": "2025-12-25",
      "paymentStatus": "DUE_TODAY",
      "rateToKrw": 1.000000,
      "convertedPriceKrw": 17000,
      "exchangeRateDate": null,
      "paymentMethod": "CARD",
      "status": "ACTIVE"
    }
  ],
  "page": 0,
  "size": 20,
  "totalCount": 1
}
```

---

## POST /api/subscriptions

구독 등록 API입니다.

범위: P0

### Request

```json
{
  "categoryId": 1,
  "name": "Netflix",
  "price": 17000,
  "currency": "KRW",
  "billingCycle": "MONTHLY",
  "billingStartDate": "2025-12-25",
  "paymentMethod": "CARD",
  "status": "ACTIVE",
  "memo": "프리미엄 요금제"
}
```

`billingStartDate`는 과거, 오늘, 미래 날짜를 모두 허용합니다. 앞으로 시작될 구독을 미리 등록할 수 있습니다.

`currency`는 `KRW`, `USD`, `JPY`, `CNY`, `EUR`를 지원합니다. `price`는 입력한 원 통화 금액 그대로 저장하며, 외화 구독은 Dashboard와 조회 응답에서 캐시된 환율 기준 KRW 환산 금액을 함께 제공합니다. `KRW`/`JPY`는 정수 입력을 권장하고, `USD`/`CNY`/`EUR`는 소수 2자리까지 허용합니다.

`status`가 `PAUSED` 또는 `CANCELED`인 상태로 등록하는 경우 `statusEffectiveDate`는 필수입니다. `statusEffectiveDate`는 `billingStartDate`보다 이전일 수 없고, MVP에서는 미래 날짜를 허용하지 않습니다.

```json
{
  "categoryId": 1,
  "name": "Netflix",
  "price": 17000,
  "currency": "KRW",
  "billingCycle": "MONTHLY",
  "billingStartDate": "2025-12-25",
  "paymentMethod": "CARD",
  "status": "CANCELED",
  "statusEffectiveDate": "2026-06-10",
  "memo": "해지 완료"
}
```

### Response data

```json
{
  "subscriptionId": 1,
  "name": "Netflix",
  "billingAnchorDay": 25,
  "billingStartDate": "2025-12-25",
  "nextPaymentDate": "2026-06-25"
}
```

서버는 `billingStartDate`의 일자를 기준으로 `billingAnchorDay`를 계산하고, `billingStartDate + billingCycle` 기준으로 현재 날짜에서 가장 가까운 `nextPaymentDate`를 계산합니다.

구독 생성 시 `subscription_status_history`에도 상태 이력을 생성합니다. `ACTIVE` 등록은 `billingStartDate`부터 열린 이력으로 저장하고, `PAUSED`/`CANCELED` 등록은 `statusEffectiveDate` 이전 기간을 `ACTIVE` 이력으로 보존할 수 있습니다.

---

## GET /api/subscriptions/{subscriptionId}

구독 상세 조회 API입니다.

범위: P0

### Response data

```json
{
  "subscriptionId": 1,
  "categoryId": 1,
  "categoryName": "영상",
  "name": "Netflix",
  "price": 17000,
  "currency": "KRW",
  "billingCycle": "MONTHLY",
  "billingAnchorDay": 25,
  "billingStartDate": "2025-12-25",
  "nextPaymentDate": "2026-06-25",
  "statusEffectiveDate": "2025-12-25",
  "paymentStatus": "DUE_TODAY",
  "rateToKrw": 1.000000,
  "convertedPriceKrw": 17000,
  "exchangeRateDate": null,
  "paymentMethod": "CARD",
  "status": "ACTIVE",
  "memo": "프리미엄 요금제"
}
```

### 예외

| 상황 | HTTP |
|---|---|
| 존재하지 않는 구독 | 404 |
| 다른 회원의 구독 접근 | 403 |
| 삭제된 구독 접근 | 404 |

---

## PUT /api/subscriptions/{subscriptionId}

구독 수정 API입니다.

범위: P0

### Request

```json
{
  "categoryId": 1,
  "name": "Netflix",
  "price": 17000,
  "currency": "KRW",
  "billingCycle": "MONTHLY",
  "billingStartDate": "2025-12-25",
  "paymentMethod": "CARD",
  "status": "ACTIVE",
  "memo": "스탠다드 요금제"
}
```

`billingStartDate`가 변경되면 `billingAnchorDay`와 현재 기준 `nextPaymentDate`를 다시 계산합니다. `billingStartDate`는 미래 날짜도 허용합니다. `status`가 `PAUSED` 또는 `CANCELED`로 변경되는 경우 `statusEffectiveDate`는 필수이며, 서버는 기존 열린 상태 이력을 종료하고 새 상태 이력을 생성합니다. 이미 `PAUSED` 또는 `CANCELED` 상태인 구독의 `statusEffectiveDate`를 수정하면 현재 열린 상태 이력의 시작일과 직전 ACTIVE 이력 종료일을 함께 보정합니다.

---

## DELETE /api/subscriptions/{subscriptionId}

구독 소프트 삭제 API입니다.

범위: P0

### Response data

```json
{
  "subscriptionId": 1,
  "deleted": true
}
```

실제 삭제가 아니라 `deleted_at = NOW()`로 처리합니다. DELETE는 잘못 입력한 구독 데이터를 목록과 대시보드에서 제외하는 동작이며, 실제 구독 종료는 `status = CANCELED`로 처리합니다.

---

## 다중 통화와 KRW 환산

MVP는 구독 원 금액과 통화를 그대로 저장하고, 조회/대시보드 계산 시 KRW 환산 금액을 함께 제공합니다.

지원 통화:

```txt
KRW, USD, JPY, CNY, EUR
```

환율은 백엔드가 Frankfurter API를 호출해 `exchange_rate` 테이블에 캐시합니다. 같은 날 이미 조회한 `provider = FRANKFURTER` 통화 row만 최신 캐시로 사용하고, 외부 API 실패 시에만 가장 최근 캐시를 fallback으로 사용합니다. `SYSTEM` seed는 fallback 전용이며 오늘 최신 캐시로 취급하지 않습니다. `KRW -> KRW` 환율은 코드에서 `1.000000`으로 처리하며 외부 API를 호출하지 않습니다.

`payment_history`와 결제 완료 처리는 MVP에서 구현하지 않습니다. SubTrack의 Dashboard는 사용자가 등록한 구독 정보 기반의 예상 구독료 흐름을 보여줍니다.

---

## 6. 대시보드 API

## GET /api/dashboard/summary

월별 대시보드 요약 API입니다.

월간 예상 금액은 `billingStartDate + billingCycle` 반복 규칙과 `subscription_status_history`를 기준으로 선택 월에 ACTIVE 상태였던 결제 발생분만 합산합니다. 모든 금액 합계는 KRW 환산 기준입니다.

범위: P0

### Query

```txt
yearMonth=2026-06
```

### Response data

```json
{
  "yearMonth": "2026-06",
  "activeSubscriptionCount": 5,
  "monthlySubscriptionCount": 5,
  "monthlyExpectedAmount": 57610,
  "monthlyExpectedAmountKrw": 57610,
  "currency": "KRW",
  "upcomingCount": 2,
  "dueTodayCount": 1,
  "todayPaymentCount": 1
}
```

필드 의미:

| 필드 | 의미 |
|---|---|
| activeSubscriptionCount | 선택 월에 결제가 발생하고 해당 결제 발생일이 ACTIVE 상태 이력에 포함되는 구독 수 |
| monthlySubscriptionCount | `activeSubscriptionCount`와 같은 의미의 호환 필드 |
| monthlyExpectedAmount | 선택 월 결제 발생일이 ACTIVE 상태 이력에 포함되는 구독료 합계, KRW 환산 기준 |
| monthlyExpectedAmountKrw | `monthlyExpectedAmount`와 같은 값의 명시적 KRW 필드 |
| currency | 합계 표시 통화, 현재는 KRW |
| upcomingCount | 현재 기준 오늘부터 7일 이내 `nextPaymentDate`가 있는 ACTIVE 구독 수 |
| dueTodayCount | 현재 기준 오늘 결제 예정인 ACTIVE 구독 수 |
| todayPaymentCount | `dueTodayCount`와 같은 의미의 호환 필드 |

MVP에서는 `payment_history` 기반 결제 완료 여부를 추적하지 않으므로 미납 상태를 판단하지 않습니다.

---

## GET /api/dashboard/upcoming

다가오는 결제 목록 API입니다.

범위: P0

### Query

```txt
days=7
```

### Response data

```json
[
  {
    "subscriptionId": 1,
    "name": "Netflix",
    "categoryId": 1,
    "categoryName": "영상",
    "price": 17000,
    "currency": "KRW",
    "rateToKrw": 1.000000,
    "convertedPriceKrw": 17000,
    "exchangeRateDate": null,
    "nextPaymentDate": "2026-06-20",
    "paymentStatus": "DUE_SOON"
  }
]
```

---

## GET /api/dashboard/category-expenses

카테고리별 결제 예정액 API입니다.

카테고리별 금액은 `billingStartDate + billingCycle` 반복 규칙과 `subscription_status_history`를 기준으로 선택 월에 ACTIVE 상태였던 결제 발생분을 카테고리별로 합산합니다. 금액은 KRW 환산 기준입니다.

범위: P0

### Query

```txt
yearMonth=2026-06
```

### Response data

```json
[
  {
    "categoryId": 1,
    "categoryName": "영상",
    "colorCode": "#E53935",
    "icon": "movie",
    "totalAmount": 27000,
    "totalAmountKrw": 27000,
    "subscriptionCount": 2
  },
  {
    "categoryId": 2,
    "categoryName": "음악",
    "colorCode": "#8E24AA",
    "icon": "music_note",
    "totalAmount": 12000,
    "totalAmountKrw": 12000,
    "subscriptionCount": 1
  }
]
```

---

## GET /api/dashboard/monthly-expenses

월별 예상 구독료 추이 API입니다.

범위: P0

### Query

```txt
from=2026-01
to=2026-12
```

`from`, `to`는 필수이며 `YYYY-MM` 형식입니다. 조회 범위는 최대 24개월입니다.

### Response data

```json
{
  "from": "2026-01",
  "to": "2026-12",
  "currency": "KRW",
  "items": [
    {
      "yearMonth": "2026-01",
      "expectedAmountKrw": 42900,
      "subscriptionCount": 3
    },
    {
      "yearMonth": "2026-02",
      "expectedAmountKrw": 53800,
      "subscriptionCount": 4
    }
  ]
}
```

각 월은 Dashboard summary의 월간 예상 구독료와 같은 기준으로 계산합니다. 데이터가 없는 월도 `expectedAmountKrw = 0`, `subscriptionCount = 0`으로 포함합니다.

---

## GET /api/dashboard/monthly-schedule

선택 월 결제 예정 목록 API입니다.

범위: P0

### Query

```txt
yearMonth=2026-06
```

### Response data

```json
{
  "yearMonth": "2026-06",
  "items": [
    {
      "subscriptionId": 1,
      "name": "ChatGPT Plus",
      "categoryId": 5,
      "categoryName": "인공지능 도구",
      "colorCode": "#7C3AED",
      "icon": "ai",
      "paymentDate": "2026-06-26",
      "price": 20.00,
      "currency": "USD",
      "rateToKrw": 1380.500000,
      "convertedPriceKrw": 27610,
      "exchangeRateDate": "2026-06-26",
      "billingCycle": "MONTHLY",
      "paymentMethod": "Visa"
    }
  ]
}
```

이 API는 현재 기준 upcoming이 아니라 선택한 월 전체의 반복 결제 예정 목록입니다. 선택 월의 결제 발생일이 ACTIVE 상태 이력 기간에 포함되는 구독만 반환합니다.

---

## 7. 알림 API

## GET /api/notifications

내부 알림 목록 조회 API입니다.

범위: P1

### Query

```txt
isRead=false
page=0
size=20
```

### Response data

```json
{
  "content": [
    {
      "notificationId": 1,
      "notificationType": "PAYMENT_DUE_TOMORROW",
      "title": "내일 결제 예정",
      "message": "Netflix 결제가 내일 예정되어 있습니다.",
      "subscriptionId": 1,
      "scheduledDate": "2026-06-20",
      "isRead": false,
      "createdAt": "2026-06-19T09:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1
}
```

---

## PATCH /api/notifications/{notificationId}/read

알림 단건 읽음 처리 API입니다.

범위: P1

### Response data

```json
{
  "notificationId": 1,
  "isRead": true
}
```

---

## PATCH /api/notifications/read-all

알림 전체 읽음 처리 API입니다.

범위: P1

### Response data

```json
{
  "updatedCount": 5
}
```
