# 로컬 개발 환경 실행 가이드

이 문서는 SubTrack 백엔드를 로컬 Docker MySQL과 함께 실행하는 순서를 정리합니다.

## 1. 환경 변수 파일 준비

루트의 `.env.example`을 참고해서 `.env` 파일을 만듭니다.

```bash
DB_URL=jdbc:mysql://localhost:3306/subtrack?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME=root
DB_PASSWORD=password
JWT_SECRET=change-this-secret-key
ONESIGNAL_APP_ID=
ONESIGNAL_REST_API_KEY=
```

`.env` 파일은 개인 개발 환경 값이 들어가므로 Git에 올리지 않습니다.

## 2. Docker MySQL 실행

프로젝트 루트에서 MySQL 컨테이너를 실행합니다.

```bash
docker compose up -d mysql
```

컨테이너 상태를 확인합니다.

```bash
docker compose ps
```

## 3. schema.sql 실행

DB 테이블을 생성합니다.

```bash
docker exec -i subtrack-mysql mysql -uroot -ppassword subtrack < database/01_schema.sql
```

`.env`에서 `DB_PASSWORD`를 바꿨다면 `-ppassword` 부분도 같은 값으로 바꿔서 실행합니다.

## 4. seed_category.sql 실행

기본 구독 카테고리 데이터를 넣습니다.

```bash
docker exec -i subtrack-mysql mysql -uroot -ppassword subtrack < database/02_seed_category.sql
```

## 5. 백엔드 실행

백엔드 폴더로 이동해서 Spring Boot를 실행합니다.

```bash
cd backend
mvn spring-boot:run
```

로컬 기본 접속 정보는 `backend/src/main/resources/application.yml`에 기본값으로 들어가 있습니다.
배포 환경에서는 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` 같은 환경변수를 주입해서 값을 바꿉니다.

## 6. Health Check 확인

백엔드 실행 후 다음 주소를 확인합니다.

```bash
curl http://localhost:8080/api/health
```

정상 응답 예시는 다음과 같습니다.

```json
{
  "success": true,
  "message": "요청이 성공했습니다.",
  "data": "SubTrack backend is running"
}
```

## 7. MySQL 중지

개발을 마치면 컨테이너를 중지할 수 있습니다.

```bash
docker compose down
```

DB 데이터까지 삭제하려면 named volume도 함께 삭제합니다.

```bash
docker compose down -v
```
