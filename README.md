# SubTrack

SubTrack은 사용자가 매달 또는 매년 반복해서 결제하는 구독 서비스를 등록하고, 결제일·금액·카테고리·결제수단·월별 결제 예정액을 관리할 수 있는 개인 구독 관리 웹 서비스입니다.

> SubTrack은 사용자의 반복 결제 항목을 체계적으로 관리하고, 결제 예정일과 월별 결제 예정 현황을 대시보드와 알림으로 제공하는 구독 관리 웹 서비스입니다.

---

## 프로젝트 목적

SubTrack은 단순 가계부가 아니라 반복 결제 관리에 초점을 둔 포트폴리오 프로젝트입니다.

주요 목표는 다음과 같습니다.

- 반복 결제 데이터 관리
- 다음 결제 예정일 계산
- D-Day 및 결제 예정 상태 계산
- 대시보드 집계
- 결제 완료 이력 관리
- 중복 결제 완료 방지
- 사용자별 데이터 권한 검증
- 결제 하루 전 알림 자동화

신입 풀스택/백엔드 개발자 포트폴리오용으로, 과한 엔터프라이즈 구조보다 직접 이해하고 설명 가능한 구조를 목표로 합니다.

---

## 기술 스택

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

## 주요 기능

### P0 - MVP

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
- 결제 예정 목록
- 카테고리별 결제 예정액
- Recharts 기반 시각화
- 반응형 UI
- Swagger 문서화

### P1 - 확장 기능

- 결제 이력 관리
- 결제 완료 처리
- 결제 완료 중복 방지
- 다음 결제일 갱신
- OneSignal 결제 하루 전 알림
- 내부 알림 목록
- 월별 실제 지출 추이
- 푸시 설정
- 일부 테스트 코드
- 배포 또는 배포 가이드

### P2 - 보류 기능

- 관리자 기능
- 공유 구독방
- 실시간 채팅
- 소셜 로그인
- 커스텀 카테고리 고도화
- 복잡한 반복 결제 주기
- 월간 요약 푸시
- 다중 기기 푸시 관리

---

## 프로젝트 구조

```txt
SubTrack/
 ├─ AGENTS.md
 ├─ README.md
 ├─ docs/
 │   ├─ project-spec.md
 │   ├─ erd.md
 │   ├─ api-spec.md
 │   └─ setup-plan.md
 ├─ database/
 │   ├─ 01_schema.sql
 │   └─ 02_seed_category.sql
 ├─ backend/
 └─ frontend/
```

---

## 개발 환경 방향

개발 초기에는 로컬 Docker MySQL을 사용합니다.

집과 학원처럼 여러 환경에서 개발할 수 있으므로, DB 자체를 공유하기보다 SQL 파일로 DB 구조를 재현할 수 있도록 구성합니다.

```txt
database/
 ├─ 01_schema.sql
 └─ 02_seed_category.sql
```

배포 또는 시연 단계에서는 외부 MySQL 호스팅이나 AWS RDS MySQL로 datasource 설정만 변경할 수 있도록 구성합니다.

---

## 핵심 설계 포인트

### 1. 결제 기준일

`billing_anchor_day`는 반복 결제일 계산의 기준일입니다.

사용자가 입력한 `next_payment_date`의 일자를 기준값으로 사용합니다.

예시:

```txt
2026-01-31 등록
billing_anchor_day = 31

결제 예정일 흐름:
2026-01-31 -> 2026-02-28 -> 2026-03-31
```

### 2. 결제 예정 상태

결제 예정 상태는 DB에 저장하지 않습니다.

응답 시점에 `next_payment_date`와 오늘 날짜를 비교하여 계산합니다.

```txt
UPCOMING   : 8일 이후
DUE_SOON   : 1~7일 이내
DUE_TODAY  : 오늘
OVERDUE    : 오늘보다 이전
```

### 3. 결제 완료 중복 방지

결제 완료 API는 중복 클릭 또는 네트워크 재요청에 대비해야 합니다.

`payment_history` 테이블에 다음 UNIQUE 제약을 둡니다.

```sql
UNIQUE(subscription_id, scheduled_payment_date)
```

동시 요청 상황에서 중복 INSERT가 발생하면 `DataIntegrityViolationException`을 잡아 `409 Conflict`로 응답합니다.

### 4. 소프트 삭제

구독 삭제는 물리 삭제가 아니라 `deleted_at`을 세팅하는 방식으로 처리합니다.

삭제된 구독은 목록, 대시보드, 알림 대상에서 제외하지만 과거 결제 이력은 유지합니다.

---

## 실행 방법

백엔드와 프론트엔드 초기 세팅 후 작성합니다.

작성 예정 항목:

- Backend 실행 방법
- Frontend 실행 방법
- Docker MySQL 실행 방법
- Swagger 접속 주소
- 환경변수 설정 방법

---

## 문서

- [프로젝트 기획](docs/project-spec.md)
- [ERD 설계](docs/erd.md)
- [API 명세](docs/api-spec.md)
- [프로젝트 세팅 계획](docs/setup-plan.md)
