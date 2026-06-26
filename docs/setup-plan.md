# SubTrack Setup Plan

## 1. 전체 개발 순서

SubTrack은 다음 순서로 개발합니다.

1. DB DDL 작성
2. 백엔드 초기 세팅
3. 공통 응답 / 예외 처리
4. JWT / Security 세팅
5. 회원가입 / 로그인 / 내 정보 조회
6. 구독 CRUD
7. 구독 상태 이력 관리
8. D-Day / 결제 예정 상태 계산
9. 대시보드 API
10. 프론트 초기 세팅
11. 로그인 / 회원가입 화면
12. 구독 목록 / 등록 / 수정 / 상세 화면
13. 대시보드 화면
14. P1 결제 완료 / 알림 / 테스트

---

# Backend

## 2. 백엔드 프로젝트 구조

이전 프로젝트의 Controller / Service / Dao / mapper.xml 흐름을 유지합니다.

패키지 루트는 `com.subtrack`을 사용합니다.

```txt
backend/
 └─ src/main/java/com/subtrack/
     ├─ SubTrackApplication.java
     ├─ global/
     │   ├─ config/
     │   │   ├─ SecurityConfig.java
     │   │   ├─ CorsConfig.java
     │   │   ├─ SwaggerConfig.java
     │   │   └─ MyBatisConfig.java
     │   ├─ security/
     │   │   ├─ JwtTokenProvider.java
     │   │   ├─ JwtAuthenticationFilter.java
     │   │   ├─ CustomUserDetails.java
     │   │   └─ CustomUserDetailsService.java
     │   ├─ exception/
     │   │   ├─ GlobalExceptionHandler.java
     │   │   ├─ ErrorCode.java
     │   │   └─ BusinessException.java
     │   ├─ response/
     │   │   ├─ ApiResponse.java
     │   │   └─ ErrorResponse.java
     │   └─ util/
     │       ├─ DateCalculator.java
     │       └─ PaymentStatusCalculator.java
     └─ domain/
         ├─ auth/
         │   ├─ controller/
         │   ├─ service/
         │   └─ dto/
         ├─ member/
         │   ├─ controller/
         │   ├─ service/
         │   ├─ dao/
         │   ├─ dto/
         │   └─ vo/
         ├─ category/
         ├─ subscription/
         ├─ payment/
         ├─ dashboard/
         ├─ notification/
         └─ push/
```

---

## 3. MyBatis 구조

이전 프로젝트처럼 Dao 인터페이스와 XML mapper를 사용합니다.

```txt
src/main/java/com/subtrack/domain/subscription/dao/
 └─ SubscriptionDao.java

src/main/resources/mapper/subscription/
 └─ SubscriptionMapper.xml
```

주요 Dao:

```txt
MemberDao
CategoryDao
SubscriptionDao
SubscriptionStatusHistoryDao
PaymentHistoryDao
DashboardDao
NotificationDao
PushLogDao
```

---

## 4. application.yml 설정 항목

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/subtrack?serverTimezone=Asia/Seoul&characterEncoding=UTF-8}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis:
  mapper-locations: classpath:/mapper/**/*.xml
  type-aliases-package: com.subtrack.domain
  configuration:
    map-underscore-to-camel-case: true

jwt:
  secret: ${JWT_SECRET}
  access-token-expiration-ms: 3600000

onesignal:
  app-id: ${ONESIGNAL_APP_ID:}
  rest-api-key: ${ONESIGNAL_REST_API_KEY:}

scheduler:
  payment-reminder:
    enabled: true
    cron: "0 0 9 * * *"

springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

---

## 5. JWT / Security 설정 순서

1. Member 회원가입 구현
2. BCrypt 비밀번호 암호화 적용
3. 로그인 성공 시 JWT 발급
4. JwtTokenProvider 작성
5. JwtAuthenticationFilter 작성
6. SecurityConfig에서 stateless 설정
7. `/api/auth/**`, Swagger 경로 permitAll
8. 나머지 `/api/**` 인증 필요 설정
9. 컨트롤러에서 로그인 사용자 memberId 사용
10. 401 / 403 예외 응답 통일

MVP에서는 refresh token, Redis, token blacklist를 구현하지 않습니다.

---

## 6. 전역 예외 처리

`@RestControllerAdvice`를 사용합니다.

처리 대상:

```txt
MethodArgumentNotValidException -> 400
ConstraintViolationException -> 400
BusinessException -> ErrorCode 기준 응답
AccessDeniedException -> 403
AuthenticationException -> 401
DataIntegrityViolationException -> 409
Exception -> 500
```

P1 결제 완료 중복 처리에서 `DataIntegrityViolationException`을 409 Conflict로 변환하는 것이 중요합니다.

---

## 7. 백엔드 코드 스타일

기존 WithDay 스타일을 유지하되, 다음처럼 정리합니다.

### Controller

- 요청을 받고 Service를 호출합니다.
- 비즈니스 로직을 넣지 않습니다.
- try-catch를 반복하지 않습니다.
- 응답은 ApiResponse로 감쌉니다.

### Service

- 핵심 검증과 비즈니스 로직을 처리합니다.
- DB 변경이 있는 메서드에는 `@Transactional`을 사용합니다.
- 권한 검증은 Service에서 명확히 처리합니다.

### Dao

- MyBatis mapper.xml과 연결되는 인터페이스입니다.
- SQL 로직을 직접 작성하지 않습니다.

### mapper.xml

- 실제 SQL을 작성합니다.
- `deleted_at IS NULL` 조건을 명확히 넣습니다.
- 검색 / 필터에는 MyBatis 동적 SQL을 사용합니다.

---

## 8. 테스트 우선순위

전체 테스트를 과하게 작성하지 않고, 핵심 정책 위주로 작성합니다.

1. 다음 결제일 계산 테스트
2. `billing_anchor_day = 31` 케이스 테스트
3. 결제 완료 시 payment_history 저장 테스트
4. 결제 완료 시 next_payment_date 갱신 테스트
5. 중복 결제 완료 시 409 응답 테스트
6. 다른 사용자의 구독 접근 차단 테스트
7. 대시보드 집계 테스트

---

# Frontend

## 9. 프론트 프로젝트 구조

이전 프로젝트와 맞추기 위해 `src/page`를 사용합니다.

```txt
frontend/
 └─ src/
     ├─ app/
     │   ├─ App.jsx
     │   ├─ router.jsx
     │   └─ provider/
     │       ├─ QueryProvider.jsx
     │       └─ ThemeProvider.jsx
     ├─ features/
     │   ├─ auth/
     │   │   ├─ api/
     │   │   ├─ store/
     │   │   └─ validation/
     │   ├─ member/
     │   ├─ category/
     │   ├─ subscription/
     │   │   ├─ api/
     │   │   ├─ validation/
     │   │   └─ utils/
     │   ├─ dashboard/
     │   ├─ notification/
     │   └─ payment/
     ├─ page/
     │   ├─ Landing/
     │   ├─ Auth/
     │   ├─ Dashboard/
     │   ├─ SubscriptionList/
     │   ├─ SubscriptionForm/
     │   ├─ SubscriptionDetail/
     │   ├─ Notification/
     │   └─ MyPage/
     ├─ shared/
     │   ├─ api/
     │   │   └─ api.js
     │   ├─ ui/
     │   │   ├─ Button/
     │   │   ├─ Form/
     │   │   ├─ Select/
     │   │   ├─ Card/
     │   │   └─ Badge/
     │   ├─ lib/
     │   │   ├─ dayjs.js
     │   │   └─ oneSignal.js
     │   └─ utils/
     │       ├─ format.js
     │       └─ paymentStatus.js
```

---

## 10. 라우팅 구조

```txt
/                         랜딩
/login                    로그인
/signup                   회원가입
/dashboard                대시보드
/subscriptions            구독 목록
/subscriptions/new        구독 등록
/subscriptions/:id        구독 상세
/subscriptions/:id/edit   구독 수정
/notifications            알림
/my                       마이페이지
```

인증 필요 경로:

```txt
/dashboard
/subscriptions
/subscriptions/new
/subscriptions/:id
/subscriptions/:id/edit
/notifications
/my
```

---

## 11. Axios instance 구조

이전 프로젝트처럼 Axios instance를 만들고 요청 직전에 Zustand authStore에서 토큰을 꺼내 Authorization 헤더에 붙입니다.

파일 위치:

```txt
src/shared/api/api.js
```

방향:

```js
import axios from "axios";
import { useAuthStore } from "../../features/auth/store/authStore";

const BASE_URL = import.meta.env.VITE_BACKSERVER;

export const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;

    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);
```

SubTrack에서는 백엔드 응답이 공통 응답 형태이므로, 각 API 함수에서는 필요에 따라 `response.data.data`를 꺼내서 사용할 수 있습니다.

---

## 12. React Query query key 설계

```js
["member", "me"]

["categories"]

["subscriptions", "list", filters]
["subscriptions", "detail", subscriptionId]

["dashboard", "summary", yearMonth]
["dashboard", "upcoming", days]
["dashboard", "category-expenses", yearMonth]
["dashboard", "monthly-expenses", year]

["notifications", "list", filters]
```

mutation 후 invalidate 기준:

| Mutation | invalidate |
|---|---|
| 구독 등록 | subscriptions, dashboard |
| 구독 수정 | subscriptions, subscription detail, dashboard |
| 구독 삭제 | subscriptions, dashboard |
| 결제 완료 | subscriptions, subscription detail, dashboard |
| 알림 읽음 | notifications |
| 닉네임 수정 | member me |

---

## 13. Zustand store 기준

Zustand는 최소한으로 사용합니다.

### authStore

- token
- user
- setLogin
- logout

### uiStore

- mobileMenuOpen
- selectedYearMonth

서버 데이터는 Zustand에 넣지 않습니다.  
구독 목록, 대시보드, 알림 목록은 React Query가 관리합니다.

---

## 14. Form 구조

React Hook Form + Yup을 사용합니다.

```txt
features/auth/validation/authSchema.js
features/subscription/validation/subscriptionSchema.js
features/member/validation/memberSchema.js
```

구독 등록 / 수정 검증:

| 필드 | 검증 |
|---|---|
| name | 필수, 1~100자 |
| categoryId | 필수 |
| price | 필수, 0 이상 |
| billingCycle | MONTHLY 또는 YEARLY |
| billingStartDate | 필수 날짜, 구독 시작일 또는 첫 결제일, 미래 날짜 허용 |
| statusEffectiveDate | PAUSED/CANCELED일 때 필수, billingStartDate 이상 오늘 이하 |
| paymentMethod | 필수 |
| status | ACTIVE, PAUSED, CANCELED |
| memo | 500자 이하 |

---

## 15. UI 스타일 방향

이전 프로젝트처럼 CSS Modules와 shared UI 컴포넌트를 사용합니다.

- `Button`
- `FormField`
- `Input`
- `TextArea`
- `CommonSelect`
- `Badge`
- `Card`

MUI는 다음 용도로 사용합니다.

- Snackbar
- Alert
- Modal
- Dialog
- Select 또는 Date 관련 보조 UI
- 기본 레이아웃 컴포넌트

SubTrack UI는 모바일에서 카드형 목록을 우선합니다.

---

## 16. 화면 구성

### Landing

- 서비스 소개
- 시작하기 버튼
- 로그인 / 회원가입 링크

### Login

- 이메일
- 비밀번호
- 로그인 버튼
- 회원가입 이동
- Snackbar 에러 표시

### Signup

- 이메일
- 닉네임
- 비밀번호
- 비밀번호 확인
- 회원가입 버튼

### Dashboard

- 선택 월 결제 구독 수
- 월간 구독료 합계
- 현재 기준 7일 이내 결제 수
- 오늘 결제 예정 수
- 다가오는 결제 목록
- 카테고리별 월간 구독료 차트

### SubscriptionList

- 검색창
- 카테고리 필터
- 상태 필터
- 결제 예정 상태 필터
- 구독 카드 목록
- 구독 추가 버튼

### SubscriptionForm

- 구독명
- 카테고리
- 금액
- 결제주기
- 구독 시작일 또는 첫 결제일
- 결제수단
- 상태
- 상태 적용일(PAUSED/CANCELED 변경 시)
- 메모

### SubscriptionDetail

- 구독 기본 정보
- 결제 정보
- 결제 기준일
- 수정 / 삭제 버튼
- P1 결제 완료 버튼
- P1 결제 이력 목록

### Notification

- 알림 목록
- 읽음 처리
- 전체 읽음 처리

### MyPage

- 내 정보
- 닉네임 수정
- P1 푸시 설정

---

## 17. Codex 작업 순서

Codex에게 한 번에 전체 프로젝트를 만들게 하지 않습니다.

권장 작업 순서:

1. 백엔드 Spring Boot 초기 세팅
2. 공통 응답 / 예외 처리
3. JWT / Security 기본 구조
4. 회원가입 / 로그인 / 내 정보 조회
5. 카테고리 조회
6. 구독 CRUD
7. 대시보드 API
8. 프론트 Vite 초기 세팅
9. Axios / authStore / router / provider
10. 로그인 / 회원가입 화면
11. 구독 목록 / 등록 / 수정 / 상세 화면
12. 대시보드 화면
13. P1 결제 완료
14. P1 알림 / OneSignal
15. 테스트 코드
