# SubTrack Project Spec

## 1. 프로젝트 개요

### 프로젝트명

SubTrack

### 서비스 소개

SubTrack은 사용자가 매달 또는 매년 반복해서 결제하는 구독 서비스를 등록하고, 결제일·금액·카테고리·결제수단·월별 결제 예정액을 관리할 수 있는 개인 구독 관리 웹 서비스입니다.

### 서비스 소개 문장

> SubTrack은 사용자의 반복 결제 항목을 체계적으로 관리하고, 결제 예정일과 월별 결제 예정 현황을 대시보드와 알림으로 제공하는 구독 관리 웹 서비스입니다.

---

## 2. 프로젝트 목적

SubTrack은 단순 가계부가 아니라 반복 결제 관리에 초점을 둔 포트폴리오 프로젝트입니다.

핵심 목적은 다음과 같습니다.

- 반복 결제 데이터 관리
- 다음 결제 예정일 계산
- D-Day 및 결제 예정 상태 계산
- 대시보드 집계
- 결제 완료 이력 관리
- 중복 결제 완료 방지
- 사용자별 데이터 권한 검증
- 알림 자동화

신입 풀스택/백엔드 개발자 포트폴리오용 프로젝트이므로, 구현 범위는 1~2주 안에 완성 가능한 수준으로 제한합니다.

---

## 3. 기술 스택

### Frontend

- React
- Vite
- React Router
- Zustand
- React Query
- Axios
- MUI
- React Hook Form
- Yup
- Recharts
- Day.js
- CSS Modules
- FSD-style architecture

### Backend

- Java 17
- Spring Boot
- Spring Security
- JWT
- MyBatis
- MySQL
- Spring Validation
- springdoc-openapi Swagger
- Spring Scheduler
- JUnit

### Push

- OneSignal

---

## 4. 개발 범위

## P0 - MVP 필수 기능

P0는 1주차 목표입니다.

- 회원가입
- 로그인
- 로그아웃
- JWT 인증
- 내 정보 조회
- 닉네임 수정
- 구독 항목 CRUD
- 구독 소프트 삭제
- 검색 / 필터
- D-Day / 결제 예정 상태 표시
- 대시보드 summary
- 대시보드 upcoming
- 대시보드 category-expenses
- Recharts 기반 카테고리별 결제 예정액 시각화
- 반응형 UI
- README 초안
- Swagger 문서화 병행

---

## P1 - 핵심 확장 기능

P1은 2주차 목표입니다.

- payment_history
- scheduled_payment_date
- 결제 완료 처리
- 결제 완료 중복 방지
- UNIQUE(subscription_id, scheduled_payment_date)
- DataIntegrityViolationException 발생 시 409 Conflict 응답
- 다음 결제일 갱신
- OneSignal 결제 하루 전 알림
- 내부 알림 목록
- 월별 실제 지출 추이 API
- 푸시 설정
- 테스트 코드 일부
- 배포 또는 배포 가이드

---

## P2 - 보류 기능

다음 기능은 v1.5 범위에서 제외합니다.

- 관리자 기능
- 공유 구독방
- 실시간 채팅
- 소셜 로그인
- 커스텀 카테고리 고도화
- WEEKLY, QUARTERLY, CUSTOM 반복 결제 규칙
- 월간 요약 푸시
- 다중 기기 푸시 구독 관리

---

## 5. 핵심 정책

## 5-1. billing_anchor_day 정책

`billing_anchor_day`는 반복 결제일 계산의 기준일입니다.

MVP에서는 별도 “말일 결제” 체크박스를 두지 않습니다.

사용자가 입력한 `billing_start_date`의 일자를 기본값으로 사용합니다.

예시 1:

```txt
2026-01-31 등록
billing_anchor_day = 31

결제 예정일 흐름:
2026-01-31 -> 2026-02-28 -> 2026-03-31
```

예시 2:

```txt
2026-02-28 등록
billing_anchor_day = 28

결제 예정일 흐름:
2026-02-28 -> 2026-03-28 -> 2026-04-28
```

2월 28일로 처음 등록한 구독을 자동으로 말일 구독으로 추정하지 않습니다.

---

## 5-2. 다음 결제일 계산 정책

`billing_start_date`는 사용자가 입력하는 구독 시작일 또는 첫 결제일입니다.

`billing_start_date`는 과거, 오늘, 미래 날짜를 모두 허용합니다. 사용자는 앞으로 시작될 구독을 미리 등록할 수 있습니다.

`next_payment_date`는 사용자가 직접 입력하지 않고, 서버가 `billing_start_date`와 `billing_cycle`을 기준으로 현재 날짜에서 가장 가까운 다음 결제 예정일을 계산해 저장합니다.

월간 구독은 `billing_start_date`의 일자를 anchor day로 사용해 매월 반복됩니다.

연간 구독은 `billing_start_date`와 같은 월에 매년 반복됩니다.

Dashboard 월간 구독료 합계는 `next_payment_date`가 아니라 `billing_start_date + billing_cycle` 반복 규칙과 `subscription_status_history`를 기준으로 계산합니다.

중요한 점은 P1 결제 완료 이후에도 `paid_date`가 아니라 기존 결제 주기를 기준으로 다음 결제일을 계산한다는 것입니다.

이유는 사용자가 늦게 결제 완료를 눌러도 원래 결제 주기가 밀리지 않도록 하기 위해서입니다.

제외 조건:

- 선택 월 대시보드는 반복 결제 발생일이 `ACTIVE` 상태 이력에 포함될 때만 집계
- 현재 기준 upcoming, due today, 알림 대상은 현재 `status = ACTIVE`이고 `deleted_at IS NULL`인 구독만 사용
- `deleted_at IS NOT NULL`인 구독은 목록, 대시보드, 알림 대상에서 전체 제외

---

## 5-3. 구독 상태 이력 정책

`subscription.status`는 현재 상태를 나타냅니다.

선택 월 대시보드는 현재 상태 하나만 보지 않고 `subscription_status_history`를 기준으로 해당 월의 반복 결제 발생일에 구독이 ACTIVE였는지 판단합니다.

`status_effective_date`는 PAUSED 또는 CANCELED 상태가 적용되는 날짜입니다. ACTIVE 상태에서는 사용하지 않습니다. MVP에서는 미래 예약 상태 변경을 허용하지 않으며, `billing_start_date`보다 이전일 수 없습니다.

PAUSED 또는 CANCELED 상태를 유지한 채 수정하는 경우 `status_effective_date`를 변경할 수 있습니다. 이때 현재 열린 상태 이력의 시작일과 직전 ACTIVE 이력의 종료일을 함께 보정합니다.

상태 의미:

| 상태 | 의미 |
|---|---|
| ACTIVE | 구독료가 발생하는 기간 |
| PAUSED | 일시정지 기간, 해당 기간의 결제 발생월은 대시보드에서 제외 |
| CANCELED | 해지 이후 기간, 해지 이후 결제 발생월은 대시보드에서 제외 |

예를 들어 2025-12-25에 시작한 월간 구독이 2026-06-10에 CANCELED가 되면, 2026-05까지의 과거 ACTIVE 월 통계는 유지하고 2026-06-25 이후 발생분은 제외합니다.

가격 변경 이력은 MVP에서 구현하지 않습니다. 따라서 과거 월 예상 구독료도 현재 `subscription.price`를 기준으로 계산합니다. 실제 가격 정확도는 P1 이후 `payment_history` 또는 별도 price history가 필요합니다.

MVP는 KRW 중심으로 사용하며 다중 통화 환율 변환은 구현하지 않습니다. `price`는 0 이상의 정수 금액을 허용합니다.

기본 카테고리 표시명은 사용자 화면에서 한글을 사용합니다. 기본값은 `영상`, `음악`, `클라우드`, `생산성`, `인공지능 도구`, `쇼핑`, `교육`, `금융`, `생활`, `기타`입니다.

---

## 5-4. 결제 예정 상태 표시 정책

결제 예정 상태는 DB에 저장하지 않습니다.

서버 응답 시점에 `next_payment_date`와 오늘 날짜를 비교해 계산합니다.

ACTIVE 구독의 `next_payment_date`는 조회 시 현재 날짜 기준 가장 가까운 다음 결제 예정일로 보정합니다.

MVP에서는 `payment_history` 기반 결제 완료 여부를 추적하지 않으므로 미납 상태를 판단하지 않습니다. 실제 미결제 판단은 P1 이후 결제 이력 기능에서 재검토합니다.

| 상태 | 조건 |
|---|---|
| UPCOMING | next_payment_date가 8일 이후 |
| DUE_SOON | next_payment_date가 1~7일 이내 |
| DUE_TODAY | next_payment_date가 오늘 |

---

## 5-5. 결제 완료 중복 방지 정책

결제 완료 API는 사용자가 버튼을 두 번 누르거나, 네트워크 재요청이 발생할 수 있으므로 중복 처리를 막아야 합니다.

처리 순서:

1. subscription 조회
2. 작성자 member_id 검증
3. scheduled_payment_date = subscription.next_payment_date
4. payment_history 중복 여부 확인
5. payment_history 저장
6. subscription.next_payment_date 갱신
7. 전체 과정을 하나의 트랜잭션으로 처리

중복 방지:

- payment_history에 UNIQUE(subscription_id, scheduled_payment_date)를 적용합니다.
- SELECT와 INSERT 사이에 동시 요청이 들어오면 둘 다 SELECT는 통과할 수 있습니다.
- 이 경우 INSERT 중 하나가 UNIQUE 제약 위반으로 DataIntegrityViolationException을 발생시킵니다.
- 해당 예외를 잡아 409 Conflict로 응답합니다.
- MVP에서는 비관적 락 SELECT FOR UPDATE는 적용하지 않습니다.

---

## 5-6. 구독 삭제 정책

DELETE는 물리 삭제가 아니라 `deleted_at`을 세팅하는 소프트 삭제로 처리합니다.

목록 조회, 대시보드, 알림 대상에서는 `deleted_at IS NULL`인 항목만 사용합니다.

실제 구독 종료는 DELETE가 아니라 `status = CANCELED`로 처리합니다. DELETE는 잘못 입력한 데이터를 제거하는 행위이므로 대시보드 통계에서도 전체 제외합니다.

삭제된 구독의 과거 `payment_history`는 유지합니다.

복구 기능은 MVP 범위 밖입니다.

---

## 5-7. 알림 발송 정책

알림 대상:

- status = ACTIVE
- deleted_at IS NULL
- push_enabled = true
- next_payment_date = tomorrow

알림 처리:

1. 결제 하루 전 대상 구독 조회
2. notification 테이블에 내부 알림 저장
3. OneSignal 푸시 발송
4. push_log에 성공 또는 실패 결과 저장

push_log에는 다음 UNIQUE 제약을 적용합니다.

```txt
UNIQUE(subscription_id, notification_type, scheduled_date)
```

---

## 6. 개발 우선순위

1. DB DDL 작성
2. 백엔드 공통 응답 / 예외 / JWT 구조
3. 회원가입 / 로그인 / 내 정보 조회
4. 구독 CRUD
5. D-Day / 결제 예정 상태 계산
6. 대시보드 API
7. 프론트 초기 라우팅 / 레이아웃
8. 로그인 / 회원가입 화면
9. 구독 목록 / 등록 / 수정 / 상세 화면
10. P1 결제 완료 / 알림 / 테스트
