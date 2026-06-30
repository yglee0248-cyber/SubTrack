# SubTrack AWS Secrets and Environment Variables

이 문서는 SubTrack AWS 배포에 필요한 GitHub Repository Variables, Repository Secrets, EC2 환경변수, frontend/backend 운영 환경변수를 정리한다.

실제 secret 값, 실제 AWS ARN, 실제 비밀번호, 실제 private key는 문서와 코드에 기록하지 않는다. 아래 값은 모두 placeholder다.

## 1. 기본 원칙

- secret 값은 Git에 커밋하지 않는다.
- `.env`, `app.env`, private key, access key는 레포지토리에 저장하지 않는다.
- `VITE_`로 시작하는 frontend 환경변수는 브라우저 번들에 포함되므로 secret을 넣지 않는다.
- GitHub Actions 로그에 secret이 출력되지 않게 `echo`, `cat`, debug 출력을 주의한다.
- backend workflow는 GitHub Secrets/Variables 값을 사용해 EC2의 `/opt/subtrack/app.env`를 생성 또는 갱신한다.
- `/opt/subtrack/app.env` 내용은 workflow 로그에 출력하지 않는다.

## 2. Frontend Workflow 값

Frontend 배포 workflow:

```txt
.github/workflows/deploy-frontend.yml
```

Repository Variables:

| 이름 | 예시 | 설명 |
| --- | --- | --- |
| `AWS_REGION` | `ap-northeast-2` | S3/CloudFront 배포 리전 |
| `NODE_VERSION` | `20` | frontend build에 사용할 Node 버전 |
| `S3_BUCKET_NAME` | `<s3-bucket-name>` | build 결과물을 업로드할 S3 bucket |
| `CLOUDFRONT_DISTRIBUTION_ID` | `<cloudfront-distribution-id>` | invalidation 대상 CloudFront distribution |
| `VITE_BACKSERVER` | `https://<backend-api-domain>` | 브라우저 번들에 포함될 backend API base URL |

Repository Secrets:

| 이름 | 설명 |
| --- | --- |
| `AWS_ACCESS_KEY_ID` | S3 sync와 CloudFront invalidation 권한이 있는 AWS access key |
| `AWS_SECRET_ACCESS_KEY` | 위 access key의 secret |

`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`는 frontend S3/CloudFront 배포에서 필요하다. backend SSH 배포 workflow가 AWS CLI를 직접 사용하지 않는다면 backend 필수값으로 두지 않는다.

## 3. Backend Workflow 값

Backend 배포 workflow:

```txt
.github/workflows/deploy-backend.yml
```

Repository Secrets:

| 이름 | 필수 | 설명 |
| --- | --- | --- |
| `EC2_HOST` | 예 | EC2 public DNS 또는 접속 host |
| `EC2_USER` | 예 | SSH 접속 사용자명 |
| `EC2_SSH_PRIVATE_KEY` | 예 | EC2 SSH private key |
| `DB_URL` | 예 | RDS MySQL JDBC URL |
| `DB_USERNAME` | 예 | RDS 접속 사용자명 |
| `DB_PASSWORD` | 예 | RDS 접속 비밀번호 |
| `JWT_SECRET` | 예 | JWT 서명 secret |

Repository Variables:

| 이름 | 필수 | 기본값 | 설명 |
| --- | --- | --- | --- |
| `BACKEND_SERVICE_NAME` | 아니오 | `subtrack-backend` | systemd service 이름 |
| `EC2_APP_DIR` | 아니오 | `/opt/subtrack` | EC2 앱 배치 경로 |
| `BACKEND_JAR_NAME` | 아니오 | `subtrack-backend.jar` | EC2에 배치할 JAR 이름 |
| `EC2_SERVICE_USER` | 예 | 없음 | systemd로 Spring Boot 앱을 실행할 Linux 사용자명 |
| `CORS_ALLOWED_ORIGINS` | 예 | 없음 | 허용할 frontend origin |
| `EXCHANGE_RATE_BASE_URL` | 아니오 | `https://api.frankfurter.dev` | 환율 API base URL |

`EC2_SERVICE_USER`는 EC2에서 systemd로 Spring Boot 앱을 실행할 Linux 사용자명이다. Amazon Linux 2023 기본 사용자는 보통 `ec2-user`, Ubuntu는 `ubuntu`일 수 있으나 실제 배포에서는 앱 실행 전용 사용자를 만들 수 있다.

## 4. Backend app.env 자동 생성

backend workflow는 runner에서 임시 `app.env` 파일을 만들고 EC2에 업로드한 뒤, EC2의 `/opt/subtrack/app.env`로 복사한다.

최종 EC2 파일:

```txt
/opt/subtrack/app.env
```

최종 형식:

```env
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
DB_URL=<GitHub Secrets DB_URL>
DB_USERNAME=<GitHub Secrets DB_USERNAME>
DB_PASSWORD=<GitHub Secrets DB_PASSWORD>
JWT_SECRET=<GitHub Secrets JWT_SECRET>
JWT_ACCESS_TOKEN_EXPIRATION_MS=3600000
CORS_ALLOWED_ORIGINS=<GitHub Variables CORS_ALLOWED_ORIGINS>
EXCHANGE_RATE_BASE_URL=<GitHub Variables EXCHANGE_RATE_BASE_URL 또는 https://api.frankfurter.dev>
```

권한:

```txt
owner: EC2_SERVICE_USER
mode : 600
```

주의:

- `app.env`는 Git에 커밋하지 않는다.
- workflow는 `app.env` 내용을 `cat` 하지 않는다.
- runner의 임시 `app.env`와 SSH private key는 workflow 종료 시 삭제한다.
- EC2 `/tmp/app.env`는 `/opt/subtrack/app.env`로 복사한 뒤 삭제한다.

## 5. Frontend Production Env

SubTrack frontend는 `VITE_BACKSERVER`를 사용해 API base URL을 결정한다.

```env
VITE_BACKSERVER=https://<backend-api-domain>
```

주의:

- `VITE_BACKSERVER`는 frontend build 시점에 브라우저 번들에 포함된다.
- 배포 후 값을 바꾸려면 frontend를 다시 빌드하고 S3에 다시 업로드해야 한다.
- `VITE_` 변수에는 JWT secret, DB password, AWS key 같은 secret을 넣지 않는다.

## 6. Backend Prod Env

SubTrack backend 운영 프로필은 `SPRING_PROFILES_ACTIVE=prod`로 실행한다.

필수 운영 값:

```env
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://<rds-endpoint>:3306/subtrack?serverTimezone=Asia/Seoul&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci&characterSetResults=utf8mb4
DB_USERNAME=<rds-app-username>
DB_PASSWORD=<rds-app-password>
JWT_SECRET=<jwt-secret>
CORS_ALLOWED_ORIGINS=https://<cloudfront-domain>
```

기본값을 둘 수 있는 값:

```env
SERVER_PORT=8080
JWT_ACCESS_TOKEN_EXPIRATION_MS=3600000
EXCHANGE_RATE_BASE_URL=https://api.frankfurter.dev
```

`CORS_ALLOWED_ORIGINS`에는 CloudFront frontend origin을 넣는다. 여러 origin이 필요하면 쉼표로 구분한다.

```env
CORS_ALLOWED_ORIGINS=http://localhost:5173,https://<cloudfront-domain>
```

`CORS_ALLOWED_ORIGINS=*`는 사용하지 않는다.

## 7. 수동 배포 참고 파일

아래 파일은 수동 배포 참고용이다.

```txt
deploy/backend/app.env.example
```

현재 자동배포 기준에서는 사람이 EC2에서 직접 `/opt/subtrack/app.env`를 만들지 않는다. 단, GitHub Actions를 쓰지 않는 수동 배포나 장애 대응 시에는 이 예시 파일을 참고해 같은 형식으로 만들 수 있다.

## 8. Secret 값을 코드에 넣으면 안 되는 이유

코드, 문서, README, GitHub Actions workflow에 실제 secret 값을 넣으면 아래 문제가 생긴다.

```txt
1. Git history에 값이 남아 삭제가 어렵다.
2. GitHub fork, clone, screenshot, log를 통해 외부로 노출될 수 있다.
3. 노출 후에는 DB password, JWT secret, AWS key를 모두 회전해야 한다.
4. frontend 번들에 들어간 값은 브라우저에서 누구나 볼 수 있다.
```

따라서 실제 값은 아래 위치에서만 관리한다.

```txt
GitHub Repository Secrets
GitHub Repository Variables
AWS IAM 또는 secret storage
EC2 /opt/subtrack/app.env
로컬 개발자 개인 .env
```
