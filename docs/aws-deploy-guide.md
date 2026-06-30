# SubTrack AWS Deploy Guide

이 문서는 SubTrack을 AWS에 배포할 때 확인할 준비 사항과 GitHub Actions 배포 흐름을 정리한 가이드다.

실제 AWS 리소스 생성은 이 문서만으로 진행하지 않는다. AWS 콘솔 또는 IaC 작업 전, 비용과 보안 그룹 범위를 먼저 확인한다.

## 1. 전체 배포 구조

목표 리전:

```txt
ap-northeast-2
```

배포 목표 구조:

```txt
User Browser
  -> CloudFront
      -> S3 private bucket: Vite React 정적 파일

User Browser
  -> Backend API endpoint
      -> EC2: Spring Boot JAR + systemd
          -> RDS MySQL
```

구성 요소:

```txt
Frontend : Vite React build 결과물(frontend/dist) -> S3 -> CloudFront
Backend  : Spring Boot executable JAR -> EC2 -> systemd service
Database : RDS MySQL
CI/CD    : GitHub Actions
Region   : ap-northeast-2
```

frontend는 build 시점의 `VITE_BACKSERVER` 값을 사용해 backend API 주소를 결정한다. backend는 `SPRING_PROFILES_ACTIVE=prod`로 실행하고, 운영 값은 EC2의 `/opt/subtrack/app.env`에서 주입한다.

## 2. RDS MySQL 생성 개요

RDS는 SubTrack의 운영 데이터베이스로 사용한다.

권장 시작 설정:

```txt
Engine        : MySQL
Version       : MySQL 8.x
Region        : ap-northeast-2
DB name       : subtrack
Public access : No
Multi-AZ      : 초기 포트폴리오 배포에서는 비용을 고려해 비활성화 가능
Storage       : 최소 용량에서 시작
Charset       : utf8mb4
Collation     : utf8mb4_unicode_ci
```

신규 RDS 초기 SQL 적용 순서:

```txt
1. RDS 생성 시 DB name을 subtrack으로 준비
2. subtrack DB에 접속
3. database/00_rds_init_all.sql 실행
```

`database/00_rds_init_all.sql`은 신규 RDS의 빈 `subtrack` DB를 위한 통합 초기화 파일이다. `database/03_*` 이후 파일은 기존 로컬 DB를 유지하면서 변경분을 반영하기 위한 migration이므로, 신규 RDS에 `00_rds_init_all.sql`을 적용한 뒤 `03~06`을 이어서 적용하지 않는다.

SQL 적용 후 확인 쿼리:

```sql
SHOW TABLES;
SELECT COUNT(*) FROM subscription_category;
SELECT COUNT(*) FROM exchange_rate;
SELECT COUNT(*) FROM subscription_status_history;
```

신규 DB라면 `subscription_status_history`는 0건이어도 정상이다.

## 3. RDS 보안 그룹 개요

RDS 보안 그룹은 EC2 backend에서만 접근할 수 있게 제한한다.

Inbound 예시:

```txt
Type  : MySQL/Aurora
Port  : 3306
Source: <ec2-security-group-id>
```

주의사항:

- RDS를 `0.0.0.0/0`에 공개하지 않는다.
- EC2와 RDS가 같은 VPC 안에 있어야 보안 그룹 참조 방식으로 접근 제어하기 쉽다.
- 운영 DB 계정은 root/master 계정 대신 애플리케이션 전용 계정을 사용하는 방향을 권장한다.

## 4. EC2 생성 개요

EC2는 Spring Boot JAR를 실행하는 backend 서버로 사용한다.

권장 시작 설정:

```txt
Region       : ap-northeast-2
OS           : Amazon Linux 2023 또는 Ubuntu LTS
Runtime      : Java 17
App path     : /opt/subtrack
Service name : subtrack-backend
```

backend workflow 실행 전 EC2에는 아래 준비가 필요하다.

- Java 17 설치
- GitHub Actions runner에서 SSH 접속 가능
- `EC2_USER`가 필요한 `sudo` 권한 보유
- `EC2_SERVICE_USER` Linux 사용자 준비
- EC2에서 RDS MySQL 3306으로 접속 가능
- backend 8080 접근 또는 HTTPS reverse proxy 구성 확인

`EC2_SERVICE_USER`는 `/opt/subtrack` 접근 권한과 systemd로 앱을 실행할 권한이 필요하다. workflow는 `/opt/subtrack/app.env`와 JAR owner를 `EC2_SERVICE_USER`로 설정한다.

## 5. Backend GitHub Actions 자동배포

backend 배포 workflow:

```txt
.github/workflows/deploy-backend.yml
```

실행 방식:

- `workflow_dispatch` 수동 실행
- `main` 브랜치 push 자동 실행

자동 실행 대상 path:

```txt
backend/**
deploy/backend/**
.github/workflows/deploy-backend.yml
```

workflow는 아래 순서로 진행한다.

```txt
1. 필수 GitHub Secrets/Variables 누락 검사
2. Java 17 setup
3. backend 디렉터리에서 ./mvnw clean package
4. 생성된 JAR를 BACKEND_JAR_NAME으로 정리
5. deploy/backend/subtrack.service의 <ec2-app-user>를 EC2_SERVICE_USER로 치환
6. GitHub Secrets/Variables로 임시 app.env 생성
7. EC2 /opt/subtrack 디렉터리 생성
8. JAR, systemd service, app.env 업로드
9. /opt/subtrack/app.env owner와 권한 설정
10. JAR owner와 권한 설정
11. /etc/systemd/system/<BACKEND_SERVICE_NAME>.service 설치
12. systemctl daemon-reload, enable, restart
13. curl http://localhost:8080/api/health 확인
14. runner와 EC2 /tmp의 민감 파일 정리
```

backend workflow가 생성하는 EC2 app.env 형식:

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

`/opt/subtrack/app.env`를 EC2에서 사람이 직접 만드는 방식은 자동배포에서는 사용하지 않는다. 수동 배포를 할 때만 `deploy/backend/app.env.example`을 참고한다.

누락값이 있으면 workflow는 값 자체를 출력하지 않고, 빠진 이름만 오류로 보여준다. `DB_PASSWORD`, `JWT_SECRET`, SSH private key, `app.env` 내용은 로그에 출력하지 않는다.

## 6. Backend Secrets/Variables

backend workflow에 필요한 Repository Secrets:

```txt
EC2_HOST
EC2_USER
EC2_SSH_PRIVATE_KEY
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
```

backend workflow에 필요한 Repository Variables:

```txt
BACKEND_SERVICE_NAME
EC2_APP_DIR
BACKEND_JAR_NAME
EC2_SERVICE_USER
CORS_ALLOWED_ORIGINS
EXCHANGE_RATE_BASE_URL
```

기본값:

```txt
BACKEND_SERVICE_NAME=subtrack-backend
EC2_APP_DIR=/opt/subtrack
BACKEND_JAR_NAME=subtrack-backend.jar
SERVER_PORT=8080
JWT_ACCESS_TOKEN_EXPIRATION_MS=3600000
EXCHANGE_RATE_BASE_URL=https://api.frankfurter.dev
```

필수 Variables:

```txt
EC2_SERVICE_USER
CORS_ALLOWED_ORIGINS
```

`CORS_ALLOWED_ORIGINS`에는 CloudFront frontend origin을 넣는다.

```env
CORS_ALLOWED_ORIGINS=https://duyrjjey5ijja.cloudfront.net
```

여러 origin이 필요하면 쉼표로 구분한다.

```env
CORS_ALLOWED_ORIGINS=http://localhost:5173,https://<cloudfront-domain>
```

`CORS_ALLOWED_ORIGINS=*`는 사용하지 않는다.

## 7. EC2 backend 수동 배포 템플릿

아래 파일들은 수동 배포 또는 workflow 내부 템플릿으로 사용한다.

```txt
backend/src/main/resources/application-prod.yml
deploy/backend/app.env.example
deploy/backend/subtrack.service
deploy/backend/deploy-backend.sh
```

`deploy/backend/app.env.example`은 수동 배포 참고용이다. 현재 backend workflow는 GitHub Secrets/Variables로 `/opt/subtrack/app.env`를 자동 생성 또는 갱신한다.

수동 배포 시에는 `<ec2-app-user>`를 실제 실행 사용자로 바꾸고, `app.env`를 직접 만든 뒤 systemd를 재시작한다. 자동배포에서는 workflow가 `<ec2-app-user>`를 `EC2_SERVICE_USER` 값으로 치환한 파일만 EC2에 업로드한다.

로그 확인:

```bash
sudo systemctl status subtrack-backend --no-pager
sudo journalctl -u subtrack-backend -n 80 --no-pager
sudo journalctl -u subtrack-backend -f
```

## 8. S3 + CloudFront Frontend 배포 개요

frontend는 Vite build 결과물인 `frontend/dist`를 S3에 업로드하고 CloudFront로 제공한다.

frontend 배포 workflow:

```txt
.github/workflows/deploy-frontend.yml
```

실행 전 필요한 리소스와 설정:

- S3 bucket
- CloudFront distribution
- S3 sync와 CloudFront invalidation 권한이 있는 AWS access key
- Repository Variables: `AWS_REGION`, `NODE_VERSION`, `S3_BUCKET_NAME`, `CLOUDFRONT_DISTRIBUTION_ID`, `VITE_BACKSERVER`
- Repository Secrets: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`

`VITE_BACKSERVER`는 frontend build 시점에 브라우저 번들에 포함되므로 secret으로 취급하지 않는다.

## 9. CloudFront OAC 사용 권장

S3 bucket은 public으로 열지 않고 CloudFront Origin Access Control(OAC)을 통해서만 읽히게 구성하는 것을 권장한다.

권장 방향:

```txt
1. S3 Block Public Access 유지
2. CloudFront Origin Access Control 생성
3. CloudFront distribution에 OAC 연결
4. S3 bucket policy는 해당 CloudFront distribution만 허용
```

S3 bucket policy에는 실제 AWS ARN을 문서에 기록하지 않고 placeholder만 사용한다.

## 10. Health Check와 배포 확인

backend health check endpoint:

```txt
GET /api/health
```

예상 응답:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "status": "UP"
  }
}
```

backend workflow는 EC2 내부에서 아래 요청으로 기동을 확인한다.

```bash
curl http://localhost:8080/api/health
```

실패하면 `systemctl status`와 최근 `journalctl` 일부를 출력한다. 단, `/opt/subtrack/app.env` 내용은 출력하지 않는다.

## 11. 최초 배포 권장 순서

```txt
1. RDS 생성 및 database/00_rds_init_all.sql 적용
2. EC2 생성, Java 17 설치, SSH/sudo/보안 그룹 준비
3. GitHub backend Secrets/Variables 등록
4. backend workflow 수동 실행 또는 main push 자동 실행
5. backend /api/health 확인
6. S3 bucket 및 CloudFront distribution 생성
7. GitHub frontend Variables/Secrets 등록
8. frontend workflow 실행
9. 브라우저에서 회원가입, 로그인, 구독 등록 확인
```

RDS 초기화는 backend workflow에서 수행하지 않는다. 신규 RDS는 별도로 `database/00_rds_init_all.sql`을 적용한다.

## 12. 배포 후 확인 순서

```txt
1. RDS가 private 상태인지 확인
2. RDS 보안 그룹이 EC2 보안 그룹만 3306으로 허용하는지 확인
3. RDS에 database/00_rds_init_all.sql 적용 여부 확인
4. EC2 /opt/subtrack/app.env가 workflow로 생성되었는지 확인
5. EC2 systemd service가 active 상태인지 확인
6. backend /api/health 응답 확인
7. frontend build 결과가 S3에 업로드되었는지 확인
8. CloudFront invalidation 후 frontend 접속 확인
9. 회원가입, 로그인, 구독 등록, 목록 조회, 대시보드 조회 확인
10. 브라우저 콘솔에서 CORS 또는 mixed content 오류가 없는지 확인
11. EC2 로그에서 DB 연결 오류가 없는지 확인
```
