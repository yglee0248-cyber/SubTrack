# AGENTS.md

## Project

This repository is for **SubTrack**, a personal subscription management web service.

SubTrack helps users manage recurring subscription payments by tracking payment dates, prices, categories, payment methods, dashboard summaries, payment history, and notifications.

The goal is to build a junior full-stack/backend developer portfolio project.

Keep the implementation simple, readable, and explainable.

Do not expand the scope beyond v1.5.

---

## Main Stack

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
- FSD-style folder structure

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
- JUnit for selected tests

### Push

- OneSignal, implemented in P1

---

## Previous Project Style

The user's previous project used the following style.

### Frontend style

- FSD-style folder structure
- `src/app` for app-level router and providers
- `src/features` for feature-level API, store, validation, and business logic
- `src/page` for page components
- `src/shared` for common API, UI, utilities, and libraries
- Axios instance with request interceptor
- Zustand auth store for token and user information
- React Query for API request state
- React Hook Form and Yup for form validation
- MUI Snackbar / Alert for toast messages
- CSS Modules for page-level styling
- Shared UI components such as Button, FormField, Input, Select
- Detailed comments for learning and explanation

### Backend style

- Controller / Service / Dao / mapper.xml flow
- MyBatis XML-based SQL
- DTO / VO classes
- Service-layer validation and transaction logic
- Many comments to explain why the code exists
- `@Transactional` used in service methods for write operations

SubTrack should follow this style, but improve structure for portfolio quality.

---

## Improvement Rules

Keep the user's familiar style, but improve these points:

- Use common API response format.
- Use GlobalExceptionHandler instead of repeated try-catch in every controller.
- Use BusinessException and ErrorCode for expected business errors.
- Use DTOs for request and response.
- Do not expose database objects directly to the frontend.
- Use Spring Security context for authenticated member information.
- Keep SQL clear and readable in mapper.xml.
- Keep comments helpful but avoid writing unnecessary comments for every obvious line.

---

## Project Scope

### P0 - MVP

- Signup
- Login
- Logout
- JWT authentication
- Get my profile
- Update nickname
- Subscription CRUD
- Subscription soft delete
- Search and filter subscriptions
- D-Day and payment status calculation
- Dashboard summary
- Upcoming payment list
- Category expense summary
- Recharts-based category expense chart
- Responsive UI
- README draft
- Swagger documentation during development

### P1 - Core Extension

- payment_history
- scheduled_payment_date
- Complete payment API
- Duplicate payment prevention
- UNIQUE(subscription_id, scheduled_payment_date)
- Return 409 Conflict when DataIntegrityViolationException occurs
- Update next_payment_date after payment completion
- OneSignal payment reminder one day before payment date
- Internal notification list
- Monthly actual expense trend API
- Push setting
- Selected tests
- Deployment guide or simple deployment

### P2 - Out of Scope

Do not implement these features unless explicitly requested.

- Admin feature
- Shared subscription room
- Realtime chat
- Social login
- Advanced custom category management
- WEEKLY, QUARTERLY, CUSTOM billing cycles
- Monthly summary push
- Multi-device push subscription management

---

## Backend Rules

- Do not use JPA.
- Use MyBatis and mapper.xml.
- Use `Dao` naming because the user's previous project used Dao.
- Controllers handle request and response only.
- Services handle validation, business rules, and transactions.
- Dao interfaces define database access methods only.
- mapper.xml files contain SQL.
- Use `@Transactional` on write service methods.
- Use `@Valid` for request validation.
- Use Lombok for simple DTO and VO classes.
- Use camelCase in Java.
- Use snake_case in MySQL.
- Use Java package root: `com.subtrack`.
- Do not add new libraries without asking.
- Do not implement features outside the v1.5 scope.

---

## Frontend Rules

- Use FSD-style structure.
- Use `src/page`, not `src/pages`, to match the user's previous project style.
- Use React Query for server state.
- Use Zustand only for auth and small UI state.
- Do not store subscription list, dashboard data, or notification list in Zustand.
- API functions should be placed under `features/{domain}/api`.
- Shared Axios instance should be placed under `src/shared/api`.
- Use Axios request interceptor to attach Bearer token.
- Use React Hook Form and Yup for forms.
- Use MUI Snackbar and Alert for toast messages.
- Use CSS Modules for page or component styles.
- Use shared UI components such as Button, FormField, Input, Select.
- Use Day.js for date formatting and D-Day display.
- Use Recharts only for dashboard charts.
- Make subscription list mobile-friendly with card-based layout.
- Avoid unnecessary complex abstraction.

---

## API Response Format

All successful API responses must use this format:

```json
{
  "success": true,
  "message": "요청이 성공했습니다.",
  "data": {}
}
```

All error responses must use this format:

```json
{
  "success": false,
  "message": "에러 메시지",
  "error": {
    "code": "ERROR_CODE",
    "details": []
  }
}
```

---

## Important Business Rules

### billing_anchor_day

- `billing_anchor_day` is the base day used to calculate recurring payment dates.
- In MVP, do not add an extra end-of-month checkbox.
- Use the day of the user-entered `next_payment_date` as `billing_anchor_day`.
- Example: 2026-01-31 -> 2026-02-28 -> 2026-03-31.
- Example: 2026-02-28 -> 2026-03-28 -> 2026-04-28.
- Do not automatically assume that a subscription first registered on February 28 is an end-of-month subscription.

### Next Payment Date

- Monthly subscription: add one month based on existing `next_payment_date`.
- Yearly subscription: add one year based on existing `next_payment_date`.
- Do not calculate the next payment date from `paid_date`.
- Late payment completion must not shift the billing cycle.
- PAUSED and CANCELED subscriptions are excluded from dashboard and notification targets.
- Subscriptions with `deleted_at` are excluded from list, dashboard, and notification targets.

### Payment Status

Do not store payment status in the database.

Calculate payment status from `next_payment_date` and today's date.

- UPCOMING: next_payment_date is after 8 or more days
- DUE_SOON: next_payment_date is within 1 to 7 days
- DUE_TODAY: next_payment_date is today
- OVERDUE: next_payment_date is before today

### Complete Payment

The complete payment API must prevent duplicate payment completion.

Processing order:

1. Find subscription.
2. Check member_id ownership.
3. Set scheduled_payment_date from subscription.next_payment_date.
4. Check existing payment_history.
5. Insert payment_history.
6. Update subscription.next_payment_date.
7. Process all steps in one transaction.

Duplicate prevention:

- Add UNIQUE(subscription_id, scheduled_payment_date) to payment_history.
- If two requests pass SELECT at the same time, one INSERT may fail due to the UNIQUE constraint.
- Catch DataIntegrityViolationException and return 409 Conflict.
- Do not use SELECT FOR UPDATE in MVP.

### Soft Delete

- DELETE subscription should not physically delete the row.
- Set `deleted_at`.
- Exclude deleted subscriptions from list, dashboard, and notification targets.
- Keep past payment_history rows.
- Restore feature is out of scope.

### Notification

Notification target:

- subscription.status = ACTIVE
- subscription.deleted_at IS NULL
- member.push_enabled = true
- subscription.next_payment_date = tomorrow

After sending a notification:

- Save internal notification to notification table.
- Save OneSignal success/failure result to push_log table.
- push_log must have UNIQUE(subscription_id, notification_type, scheduled_date).

---

## After Each Task

After making changes, explain:

- Changed files
- Why each change was made
- How to run or test it
- Any remaining TODOs

Do not claim tests passed unless they were actually run.
