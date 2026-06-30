# SubTrack AWS Secrets and Environment Variables

이 문서는 SubTrack AWS 배포에 필요한 Repository Variables, Repository Secrets, EC2 환경변수, 프론트엔드/백엔드 운영 환경변수를 정리한다.

실제 secret 값, 실제 도메인, 실제 AWS ARN은 문서와 코드에 기록하지 않는다. 아래 값은 모두 placeholder다.

## 1. 기본 원칙

- secret 값은 Git에 커밋하지 않는다.
- `.env`, `app.env`, private key, access key는 레포지토리에 저장하지 않는다.
- `VITE_`로 시작하는 프론트엔드 환경변수는 브라우저 번들에 포함되므로 secret을 넣지 않는다.
- 운영 백엔드 값은 EC2 `/opt/subtrack/app.env` 또는 안전한 secret 저장소에서 주입한다.
- GitHub Actions 로그에 secret이 출력되지 않게 echo, cat, debug 출력에 주의한다.
- 실제 AWS ARN, 실제 도메인, 실제 DB endpoint는 문서에 고정 기록하지 않고 placeholder 또는 GitHub 환경값으로만 관리한다.

## 2. GitHub Repository Variables

Repository Variables에는 공개되어도 치명적이지 않은 배포 설정값을 둔다.

예시:

```env
AWS_REGION=ap-northeast-2
NODE_VERSION=<node-version>
JAVA_VERSION=17

FRONTEND_WORKING_DIRECTORY=frontend
BACKEND_WORKING_DIRECTORY=backend

S3_BUCKET_NAME=<s3-bucket-name>
CLOUDFRONT_DISTRIBUTION_ID=<cloudfront-distribution-id>

BACKEND_SERVICE_NAME=subtrack-backend
EC2_APP_DIR=/opt/subtrack
BACKEND_JAR_NAME=subtrack-backend.jar
EC2_SERVICE_USER=<ec2-service-user>

VITE_BACKSERVER=https://<backend-api-domain>
```

주의:

- `VITE_BACKSERVER`는 secret이 아니다. 빌드된 프론트엔드 JavaScript에서 확인될 수 있다.
- `EC2_SERVICE_USER`는 EC2에서 systemd로 Spring Boot 앱을 실행할 Linux 사용자명이다.
- Amazon Linux 2023 기본 사용자는 보통 `ec2-user`, Ubuntu는 `ubuntu`일 수 있으나 실제 배포에서는 앱 실행 전용 사용자를 만들 수 있다.
- 실제 도메인을 문서에 적지 않고 GitHub Variables 또는 배포 환경에서 관리한다.
- `S3_BUCKET_NAME`, `CLOUDFRONT_DISTRIBUTION_ID`도 팀 정책상 민감하게 관리하고 싶다면 Secrets로 옮길 수 있다.

## 3. GitHub Repository Secrets

Repository Secrets에는 민감한 배포 인증 정보와 운영 secret을 둔다.

AWS 인증:

```env
AWS_ROLE_TO_ASSUME=<aws-github-actions-deploy-role>
```

OIDC 대신 access key를 쓰는 경우:

```env
AWS_ACCESS_KEY_ID=<aws-access-key-id>
AWS_SECRET_ACCESS_KEY=<aws-secret-access-key>
```

EC2 SSH 배포를 쓰는 경우:

```env
EC2_HOST=<ec2-host-or-public-dns>
EC2_USER=<ec2-ssh-user>
EC2_SSH_PRIVATE_KEY=<ec2-private-key>
```

EC2 `/opt/subtrack/app.env`에 들어갈 운영 값 예시:

```env
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://<rds-endpoint>:3306/subtrack?serverTimezone=Asia/Seoul&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci&characterSetResults=utf8mb4
DB_USERNAME=<rds-app-username>
DB_PASSWORD=<rds-app-password>
JWT_SECRET=<jwt-secret>
JWT_ACCESS_TOKEN_EXPIRATION_MS=3600000
CORS_ALLOWED_ORIGINS=https://<frontend-domain>
EXCHANGE_RATE_BASE_URL=https://api.frankfurter.dev
```

주의:

- DB 비밀번호, JWT secret, SSH private key, AWS secret access key는 반드시 Secrets에 둔다.
- `DB_URL`에는 RDS endpoint가 포함되므로 팀 정책에 따라 Secrets로 관리하는 편이 안전하다.
- secret 값을 workflow 로그에 직접 출력하지 않는다.

## 4. EC2 /opt/subtrack/app.env

EC2에서 systemd가 읽는 운영 환경변수 파일이다.

경로:

```txt
/opt/subtrack/app.env
```

예시 형식:

```env
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

DB_URL=jdbc:mysql://<rds-endpoint>:3306/subtrack?serverTimezone=Asia/Seoul&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci&characterSetResults=utf8mb4
DB_USERNAME=<rds-app-username>
DB_PASSWORD=<rds-app-password>

JWT_SECRET=<jwt-secret>
JWT_ACCESS_TOKEN_EXPIRATION_MS=3600000

CORS_ALLOWED_ORIGINS=https://<frontend-domain>
EXCHANGE_RATE_BASE_URL=https://api.frankfurter.dev
```

파일 권한 권장:

```bash
sudo chown <ec2-service-user>:<ec2-service-user> /opt/subtrack/app.env
sudo chmod 600 /opt/subtrack/app.env
```

systemd unit에서는 아래처럼 참조한다.

```ini
EnvironmentFile=/opt/subtrack/app.env
```

주의:

- `app.env`는 Git에 커밋하지 않는다.
- DB 계정은 가능하면 RDS root/master 계정이 아니라 애플리케이션 전용 계정을 사용한다.
- `CORS_ALLOWED_ORIGINS`에는 운영 프론트엔드 origin만 넣는다.

## 5. frontend production env

SubTrack 프론트엔드는 `frontend/src/shared/api/api.js`에서 `VITE_BACKSERVER`를 읽어 API base URL로 사용한다.

운영 빌드에 필요한 값:

```env
VITE_BACKSERVER=https://<backend-api-domain>
```

Vite 환경변수 특징:

- `VITE_BACKSERVER`는 빌드 시점에 번들에 포함된다.
- 배포 후 값을 바꾸려면 프론트엔드를 다시 빌드하고 S3에 다시 업로드해야 한다.
- `VITE_` 변수에는 JWT secret, DB password, AWS key 같은 secret을 절대 넣지 않는다.

로컬 개발 예시와 운영 값은 분리한다.

```txt
Local      : http://localhost:8080
Production : https://<backend-api-domain>
```

## 6. backend prod env

SubTrack 백엔드는 `backend/src/main/resources/application.yml`에서 환경변수를 읽는다. 운영에서는 로컬 기본값 대신 아래 값을 반드시 주입한다.

필수 운영 환경변수:

```env
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://<rds-endpoint>:3306/subtrack?serverTimezone=Asia/Seoul&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci&characterSetResults=utf8mb4
DB_USERNAME=<rds-app-username>
DB_PASSWORD=<rds-app-password>
JWT_SECRET=<jwt-secret>
CORS_ALLOWED_ORIGINS=https://<frontend-domain>
```

선택 또는 기본값 조정 환경변수:

```env
SERVER_PORT=8080
JWT_ACCESS_TOKEN_EXPIRATION_MS=3600000
EXCHANGE_RATE_BASE_URL=https://api.frankfurter.dev
```

환경변수 용도:

```txt
SERVER_PORT                    : Spring Boot 실행 포트
DB_URL                         : RDS MySQL JDBC URL
DB_USERNAME                    : RDS 접속 사용자명
DB_PASSWORD                    : RDS 접속 비밀번호
JWT_SECRET                     : JWT 서명 secret
JWT_ACCESS_TOKEN_EXPIRATION_MS : access token 만료 시간
CORS_ALLOWED_ORIGINS           : 허용할 프론트엔드 origin
EXCHANGE_RATE_BASE_URL         : 환율 API base URL
```

운영 주의사항:

- `JWT_SECRET`은 충분히 길고 예측 불가능한 값으로 설정한다.
- `CORS_ALLOWED_ORIGINS=*`는 사용하지 않는다.
- `DB_URL`의 host는 RDS endpoint placeholder로 관리하고 실제 값은 secret 저장소나 EC2 환경에만 둔다.
- 운영 서버에서 `application.yml`에 직접 secret 값을 적지 않는다.

## 7. secret 값을 코드에 넣으면 안 되는 이유

코드, 문서, README, GitHub Actions workflow에 실제 secret 값을 넣으면 아래 문제가 생긴다.

```txt
1. Git history에 값이 남아 삭제가 어렵다.
2. GitHub fork, clone, screenshot, log를 통해 외부로 노출될 수 있다.
3. 노출 후에는 DB password, JWT secret, AWS key를 모두 회전해야 한다.
4. 프론트엔드 번들에 들어간 값은 브라우저에서 누구나 볼 수 있다.
```

따라서 실제 값은 아래 위치에서만 관리한다.

```txt
GitHub Repository Secrets
AWS IAM / secret storage
EC2 /opt/subtrack/app.env
로컬 개발자 개인 .env
```

문서에는 항상 아래처럼 placeholder만 남긴다.

```env
DB_PASSWORD=<rds-app-password>
JWT_SECRET=<jwt-secret>
AWS_ROLE_TO_ASSUME=<aws-github-actions-deploy-role>
VITE_BACKSERVER=https://<backend-api-domain>
```

## 8. CORS_ALLOWED_ORIGINS 형식

백엔드는 `application.yml`의 `app.cors.allowed-origins` 값을 읽고, 기본값은 아래처럼 유지한다.

```env
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

운영 배포에서는 CloudFront 또는 커스텀 도메인의 origin을 주입한다.

```env
CORS_ALLOWED_ORIGINS=https://<frontend-domain>
```

여러 origin을 허용해야 하면 쉼표로 구분한다.

```env
CORS_ALLOWED_ORIGINS=http://localhost:5173,https://<cloudfront-domain>
```

`CORS_ALLOWED_ORIGINS=*`는 사용하지 않는다. 백엔드는 wildcard origin을 허용하지 않도록 구성한다.

## 9. EC2 backend 배포 파일과 환경변수 연결

운영 프로필 설정 파일:

```txt
backend/src/main/resources/application-prod.yml
```

EC2 환경변수 예시 파일:

```txt
deploy/backend/app.env.example
```

EC2 systemd 서비스 템플릿:

```txt
deploy/backend/subtrack.service
```

EC2 JAR 배치 및 서비스 재시작 스크립트:

```txt
deploy/backend/deploy-backend.sh
```

EC2의 실제 운영 환경변수 파일은 아래 경로에 둔다.

```txt
/opt/subtrack/app.env
```

`/opt/subtrack/app.env`는 `deploy/backend/app.env.example`을 참고해서 만들되 실제 값은 Git에 커밋하지 않는다.

필수 값:

```env
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://<rds-endpoint>:3306/subtrack?serverTimezone=Asia/Seoul&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci&characterSetResults=utf8mb4
DB_USERNAME=<rds-app-username>
DB_PASSWORD=<rds-app-password>
JWT_SECRET=<jwt-secret>
JWT_ACCESS_TOKEN_EXPIRATION_MS=3600000
CORS_ALLOWED_ORIGINS=https://<cloudfront-domain>
EXCHANGE_RATE_BASE_URL=https://api.frankfurter.dev
```

프론트엔드는 계속 아래 이름을 사용한다.

```env
VITE_BACKSERVER=https://<backend-api-domain>
```

백엔드 CORS는 새 이름을 만들지 않고 아래 이름으로 통일한다.

```env
CORS_ALLOWED_ORIGINS=https://<cloudfront-domain>
```

실제 secret 값, 실제 RDS endpoint, 실제 CloudFront domain, 실제 AWS ARN은 이 문서에 기록하지 않는다.

## 10. GitHub Actions workflow Variables and Secrets

프론트엔드 배포 workflow:

```txt
.github/workflows/deploy-frontend.yml
```

백엔드 배포 workflow:

```txt
.github/workflows/deploy-backend.yml
```

Repository Variables:

```env
AWS_REGION=ap-northeast-2
NODE_VERSION=20
S3_BUCKET_NAME=<s3-bucket-name>
CLOUDFRONT_DISTRIBUTION_ID=<cloudfront-distribution-id>
VITE_BACKSERVER=https://<backend-api-domain>

BACKEND_SERVICE_NAME=subtrack-backend
EC2_APP_DIR=/opt/subtrack
BACKEND_JAR_NAME=subtrack-backend.jar
EC2_SERVICE_USER=<ec2-service-user>
```

Repository Secrets:

```env
AWS_ACCESS_KEY_ID=<aws-access-key-id>
AWS_SECRET_ACCESS_KEY=<aws-secret-access-key>
EC2_HOST=<ec2-host-or-public-dns>
EC2_USER=<ec2-ssh-user>
EC2_SSH_PRIVATE_KEY=<ec2-private-key>
```

EC2 `/opt/subtrack/app.env`:

```env
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
DB_URL=jdbc:mysql://<rds-endpoint>:3306/subtrack?serverTimezone=Asia/Seoul&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci&characterSetResults=utf8mb4
DB_USERNAME=<rds-app-username>
DB_PASSWORD=<rds-app-password>
JWT_SECRET=<jwt-secret>
JWT_ACCESS_TOKEN_EXPIRATION_MS=3600000
CORS_ALLOWED_ORIGINS=https://<cloudfront-domain>
EXCHANGE_RATE_BASE_URL=https://api.frankfurter.dev
```

주의사항:

- `VITE_BACKSERVER`는 frontend build 시점에 브라우저 번들에 포함된다.
- `EC2_SERVICE_USER`는 EC2에서 systemd로 Spring Boot 앱을 실행할 Linux 사용자명이다.
- Amazon Linux 2023 기본 사용자는 보통 `ec2-user`, Ubuntu는 `ubuntu`일 수 있으나 실제 배포에서는 앱 실행 전용 사용자를 만들 수 있다.
- `CORS_ALLOWED_ORIGINS`에는 CloudFront frontend origin을 넣는다.
- `/opt/subtrack/app.env`는 GitHub Actions 로그에 출력하지 않는다.
- backend workflow는 `/opt/subtrack/app.env`를 생성하지 않는다.
- 실제 secret 값, 실제 RDS endpoint, 실제 CloudFront domain, 실제 AWS ARN은 문서와 workflow에 넣지 않는다.
