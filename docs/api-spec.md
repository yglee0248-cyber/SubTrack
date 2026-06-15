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
    "name": "OTT",
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
      "categoryName": "OTT",
      "price": 17000,
      "currency": "KRW",
      "billingCycle": "MONTHLY",
      "nextPaymentDate": "2026-06-20",
      "dDay": 5,
      "paymentStatus": "DUE_SOON",
      "paymentMethod": "CARD",
      "status": "ACTIVE"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1
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
  "nextPaymentDate": "2026-06-20",
  "paymentMethod": "CARD",
  "status": "ACTIVE",
  "memo": "프리미엄 요금제"
}
```

### Response data

```json
{
  "subscriptionId": 1,
  "name": "Netflix",
  "billingAnchorDay": 20,
  "nextPaymentDate": "2026-06-20"
}
```

서버는 `nextPaymentDate`의 일자를 기준으로 `billingAnchorDay`를 계산합니다.

---

## GET /api/subscriptions/{subscriptionId}

구독 상세 조회 API입니다.

범위: P0

### Response data

```json
{
  "subscriptionId": 1,
  "categoryId": 1,
  "categoryName": "OTT",
  "name": "Netflix",
  "price": 17000,
  "currency": "KRW",
  "billingCycle": "MONTHLY",
  "billingAnchorDay": 20,
  "nextPaymentDate": "2026-06-20",
  "dDay": 5,
  "paymentStatus": "DUE_SOON",
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
  "nextPaymentDate": "2026-06-25",
  "paymentMethod": "CARD",
  "status": "ACTIVE",
  "memo": "스탠다드 요금제"
}
```

`nextPaymentDate`가 변경되면 `billingAnchorDay`도 새 날짜 기준으로 다시 계산합니다.

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

실제 삭제가 아니라 `deleted_at = NOW()`로 처리합니다.

---

## POST /api/subscriptions/{subscriptionId}/payments

결제 완료 처리 API입니다.

범위: P1

### Request

```json
{
  "paidDate": "2026-06-20"
}
```

### Response data

```json
{
  "paymentHistoryId": 1,
  "subscriptionId": 1,
  "scheduledPaymentDate": "2026-06-20",
  "paidDate": "2026-06-20",
  "amount": 17000,
  "nextPaymentDate": "2026-07-20"
}
```

### 정책

- `scheduledPaymentDate`는 클라이언트가 보내지 않습니다.
- 서버가 `subscription.next_payment_date`를 기준으로 설정합니다.
- `amount`, `currency`, `paymentMethod`도 서버가 subscription에서 가져옵니다.
- 중복 결제 완료 시 409 Conflict를 반환합니다.

---

## 6. 대시보드 API

## GET /api/dashboard/summary

월별 대시보드 요약 API입니다.

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
  "monthlyExpectedAmount": 54000,
  "dueSoonCount": 2,
  "overdueCount": 1,
  "currency": "KRW"
}
```

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
    "price": 17000,
    "nextPaymentDate": "2026-06-20",
    "dDay": 5,
    "paymentStatus": "DUE_SOON"
  }
]
```

---

## GET /api/dashboard/category-expenses

카테고리별 결제 예정액 API입니다.

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
    "categoryName": "OTT",
    "amount": 27000
  },
  {
    "categoryId": 2,
    "categoryName": "MUSIC",
    "amount": 12000
  }
]
```

---

## GET /api/dashboard/monthly-expenses

월별 실제 지출 추이 API입니다.

범위: P1

### Query

```txt
year=2026
```

### Response data

```json
[
  {
    "month": "2026-01",
    "amount": 32000
  },
  {
    "month": "2026-02",
    "amount": 45000
  }
]
```

이 API는 `payment_history.paid_date` 기준으로 집계합니다.

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
