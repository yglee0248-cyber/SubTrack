# SubTrack ERD

## 1. 테이블 목록

SubTrack v1.5에서는 다음 테이블을 사용합니다.

| 테이블 | 범위 | 역할 |
|---|---|---|
| member | P0 / P1 | 회원, 인증, 내 정보, 푸시 설정 |
| subscription_category | P0 | 기본 구독 카테고리 |
| subscription | P0 / P1 | 사용자의 구독 항목 |
| payment_history | P1 | 결제 완료 이력 |
| notification | P1 | 내부 알림 |
| push_log | P1 | OneSignal 발송 로그 |

`notice`는 P2 기능이므로 기본 설계에서 제외합니다.

---

## 2. 테이블 관계

```txt
member 1 : N subscription

subscription_category 1 : N subscription

member 1 : N payment_history
subscription 1 : N payment_history

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
| price | 결제 금액 |
| currency | 통화 |
| billing_cycle | MONTHLY, YEARLY |
| billing_anchor_day | 반복 결제 기준일 |
| billing_start_date | 구독 시작일 또는 첫 결제일 |
| next_payment_date | 다음 결제 예정일 |
| payment_method | 결제수단 |
| status | ACTIVE, PAUSED, CANCELED |
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

## 6. payment_history

P1 결제 완료 이력을 관리합니다.

| 컬럼 | 설명 |
|---|---|
| payment_history_id | PK |
| subscription_id | 구독 ID |
| member_id | 회원 ID |
| scheduled_payment_date | 원래 결제 예정일 |
| paid_date | 실제 결제 완료 처리일 |
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

`UNIQUE(subscription_id, scheduled_payment_date)`는 결제 완료 중복 처리를 막기 위한 핵심 제약입니다.

---

## 7. notification

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

## 8. push_log

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

## 9. P0 / P1 구분

### P0에서 실제로 사용하는 테이블

- member
- subscription_category
- subscription

### P1에서 추가로 사용하는 테이블

- payment_history
- notification
- push_log

P0 단계에서도 P1 테이블을 DDL에 미리 만들어둘 수 있습니다.  
다만 API와 화면 구현은 P0 완료 후 진행합니다.
