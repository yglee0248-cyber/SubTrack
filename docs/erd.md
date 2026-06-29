# SubTrack ERD

## 1. 테이블 목록

SubTrack v1.5에서는 다음 테이블을 사용합니다.

| 테이블 | 범위 | 역할 |
|---|---|---|
| member | P0 / P1 | 회원, 인증, 내 정보, 푸시 설정 |
| subscription_category | P0 | 기본 구독 카테고리 |
| subscription | P0 / P1 | 사용자의 구독 항목 |
| subscription_status_history | P0 | 구독 상태 이력 |
| exchange_rate | P0 | 외화 구독 KRW 환산용 환율 캐시 |
| payment_history | P1 이후 후보 | 실제 결제 기록 또는 실제 지출 통계 후보 |
| notification | P1 | 내부 알림 |
| push_log | P1 | OneSignal 발송 로그 |

`notice`는 P2 기능이므로 기본 설계에서 제외합니다.

---

## 2. 테이블 관계

```txt
member 1 : N subscription

subscription_category 1 : N subscription

subscription 1 : N subscription_status_history

exchange_rate는 구독과 직접 FK를 맺지 않고 currency_code 기준으로 조회

member 1 : N payment_history (P1 이후 후보)
subscription 1 : N payment_history (P1 이후 후보)

member 1 : N notification
subscription 1 : N notification

member 1 : N push_log
subscription 1 : N push_log
notification 1 : N push_log
```

---

## 3. member

회원 인증과 사용자 기본 정보를 관리합니다.

| 컬럼 | 설명 |
|---|---|
| member_id | PK |
| email | 로그인 ID |
| password_hash | BCrypt 암호화 비밀번호 |
| nickname | 닉네임 |
| role | USER |
| status | ACTIVE, INACTIVE, DELETED |
| push_enabled | P1 푸시 수신 여부 |
| onesignal_player_id | P1 OneSignal player id |
| created_at | 생성일 |
| updated_at | 수정일 |
| deleted_at | 삭제 또는 탈퇴 시점 |

제약 조건:

| 종류 | 내용 |
|---|---|
| PK | member_id |
| UNIQUE | email |
| INDEX | status, deleted_at |

---

## 4. subscription_category

기본 구독 카테고리를 관리합니다.

MVP에서는 사용자별 커스텀 카테고리를 만들지 않고, 모든 사용자가 같은 기본 카테고리를 사용합니다.

기본 카테고리 표시명은 한글을 사용합니다. seed 기준 `영상`, `음악`, `클라우드`, `생산성`, `인공지능 도구`, `쇼핑`, `교육`, `금융`, `생활`, `기타`를 제공합니다.

| 컬럼 | 설명 |
|---|---|
| category_id | PK |
| name | 카테고리명 |
| color_code | 화면 표시용 색상 |
| icon | 화면 표시용 아이콘 |
| sort_order | 정렬 순서 |
| is_default | 기본 카테고리 여부 |
| created_at | 생성일 |
| updated_at | 수정일 |

제약 조건:

| 종류 | 내용 |
|---|---|
| PK | category_id |
| UNIQUE | name |
| INDEX | sort_order |

---

## 5. subscription

사용자의 구독 항목을 관리하는 핵심 테이블입니다.

| 컬럼 | 설명 |
|---|---|
| subscription_id | PK |
| member_id | 작성자 |
| category_id | 카테고리 |
| name | 구독명 |
| price | 사용자가 입력한 원 통화 금액, DECIMAL(12,2) |
| currency | 통화 코드: KRW, USD, JPY, CNY, EUR |
| billing_cycle | MONTHLY, YEARLY |
| billing_anchor_day | 반복 결제 기준일 |
| billing_start_date | 구독 시작일 또는 첫 결제일 |
| next_payment_date | 현재 날짜 기준 다음 결제 예정일 |
| payment_method | 결제수단 |
| status | 현재 상태: ACTIVE, PAUSED, CANCELED |
| memo | 메모 |
| created_at | 생성일 |
| updated_at | 수정일 |
| deleted_at | 소프트 삭제 시점 |

제약 조건:

| 종류 | 내용 |
|---|---|
| PK | subscription_id |
| FK | member_id -> member.member_id |
| FK | category_id -> subscription_category.category_id |
| INDEX | member_id, deleted_at, next_payment_date |
| INDEX | member_id, billing_start_date |
| INDEX | member_id, status, deleted_at |
| INDEX | member_id, category_id |
| INDEX | member_id, name |

---

## 6. subscription_status_history

구독 상태 이력을 관리합니다.

`subscription.status`는 현재 상태를 빠르게 조회하기 위한 값이고, 대시보드의 선택 월 구독료 계산은 이 테이블의 상태 기간을 기준으로 판단합니다.

| 컬럼 | 설명 |
|---|---|
| status_history_id | PK |
| subscription_id | 구독 ID |
| status | ACTIVE, PAUSED, CANCELED |
| effective_start_date | 상태 적용 시작일 |
| effective_end_date | 상태 적용 종료일, 열린 이력은 NULL |
| created_at | 생성일 |

제약 조건:

| 종류 | 내용 |
|---|---|
| PK | status_history_id |
| FK | subscription_id -> subscription.subscription_id |
| CHECK | status IN ('ACTIVE', 'PAUSED', 'CANCELED') |
| CHECK | effective_end_date IS NULL OR effective_start_date <= effective_end_date |
| INDEX | subscription_id, effective_start_date, effective_end_date |
| INDEX | subscription_id, status, effective_start_date, effective_end_date |

MySQL에서는 subscription별 열린 이력 1개만 허용하는 partial unique index를 단순하게 적용하기 어렵기 때문에, 열린 이력 관리는 Service 트랜잭션에서 보장합니다.

PAUSED 또는 CANCELED 상태를 유지한 채 `effective_start_date`를 수정하면, 현재 열린 이력의 시작일과 직전 ACTIVE 이력의 종료일을 함께 보정합니다.

---

## 7. payment_history

P1 이후 실제 결제 이력 또는 실제 지출 통계가 필요할 때 사용할 후보 테이블입니다. 현재 MVP API와 Dashboard 예상 지출 계산에서는 사용하지 않습니다.

| 컬럼 | 설명 |
|---|---|
| payment_history_id | PK |
| subscription_id | 구독 ID |
| member_id | 회원 ID |
| scheduled_payment_date | 원래 결제 예정일 |
| paid_date | 실제 결제 기록일 |
| amount | 결제 금액 |
| currency | 통화 |
| payment_method | 결제수단 |
| created_at | 생성일 |
| updated_at | 수정일 |

제약 조건:

| 종류 | 내용 |
|---|---|
| PK | payment_history_id |
| FK | subscription_id -> subscription.subscription_id |
| FK | member_id -> member.member_id |
| UNIQUE | subscription_id, scheduled_payment_date |
| INDEX | member_id, paid_date |
| INDEX | member_id, scheduled_payment_date |

`UNIQUE(subscription_id, scheduled_payment_date)`는 향후 같은 예정일의 실제 결제 기록 중복을 막기 위한 제약입니다.

---

## 8. exchange_rate

외화 구독의 KRW 환산을 위한 환율 캐시 테이블입니다.

| 컬럼 | 설명 |
|---|---|
| exchange_rate_id | PK |
| currency_code | 원 통화 코드: USD, JPY, CNY, EUR, KRW |
| target_currency | 환산 대상 통화, MVP에서는 KRW |
| rate_to_krw | 1 원 통화가 몇 KRW인지 나타내는 환율 |
| rate_date | 환율 제공자가 내려준 기준일 |
| provider | FRANKFURTER 또는 SYSTEM |
| fetched_at | 서버가 환율을 조회하거나 캐시한 시각 |
| created_at | 생성일 |
| updated_at | 수정일 |

제약 조건:

| 종류 | 내용 |
|---|---|
| PK | exchange_rate_id |
| UNIQUE | currency_code, target_currency, rate_date, provider |
| INDEX | currency_code, target_currency, fetched_at |

오늘 캐시 여부는 `rate_date`가 아니라 `fetched_at` 기준으로 판단합니다. 주말/공휴일에는 provider의 `rate_date`가 오늘이 아닐 수 있기 때문입니다.

---

## 9. notification

P1 내부 알림 목록을 관리합니다.

| 컬럼 | 설명 |
|---|---|
| notification_id | PK |
| member_id | 알림 수신 회원 |
| subscription_id | 관련 구독 |
| notification_type | 알림 타입 |
| scheduled_date | 알림 기준 결제 예정일 |
| title | 알림 제목 |
| message | 알림 내용 |
| is_read | 읽음 여부 |
| read_at | 읽은 시점 |
| created_at | 생성일 |

제약 조건:

| 종류 | 내용 |
|---|---|
| PK | notification_id |
| FK | member_id -> member.member_id |
| FK | subscription_id -> subscription.subscription_id |
| UNIQUE | member_id, subscription_id, notification_type, scheduled_date |
| INDEX | member_id, is_read, created_at |

---

## 10. push_log

P1 OneSignal 발송 결과를 관리합니다.

| 컬럼 | 설명 |
|---|---|
| push_log_id | PK |
| member_id | 회원 ID |
| subscription_id | 구독 ID |
| notification_id | 내부 알림 ID |
| notification_type | 알림 타입 |
| scheduled_date | 알림 기준 결제 예정일 |
| target_player_id | OneSignal player id |
| send_status | SUCCESS, FAILED |
| http_status_code | OneSignal 응답 코드 |
| request_body | 요청 JSON |
| response_body | 응답 JSON |
| error_message | 실패 사유 |
| sent_at | 발송 시점 |
| created_at | 생성일 |

제약 조건:

| 종류 | 내용 |
|---|---|
| PK | push_log_id |
| FK | member_id -> member.member_id |
| FK | subscription_id -> subscription.subscription_id |
| FK | notification_id -> notification.notification_id |
| UNIQUE | subscription_id, notification_type, scheduled_date |
| INDEX | member_id, scheduled_date |
| INDEX | send_status, created_at |

---

## 11. P0 / P1 구분

### P0에서 실제로 사용하는 테이블

- member
- subscription_category
- subscription
- subscription_status_history
- exchange_rate

### P1에서 추가로 사용하는 테이블

- payment_history
- notification
- push_log

P0 단계에서도 P1 테이블을 DDL에 미리 만들어둘 수 있습니다.  
다만 API와 화면 구현은 P0 완료 후 진행합니다.
