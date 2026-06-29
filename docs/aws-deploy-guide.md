# SubTrack AWS Deploy Guide

이 문서는 SubTrack을 AWS에 배포할 때 확인할 준비 사항을 정리한 가이드다.

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

프론트엔드는 빌드 시점의 `VITE_BACKSERVER` 값을 사용해 백엔드 API 주소를 결정한다.

백엔드는 `application.yml`의 로컬 기본값을 그대로 사용하지 않고, EC2의 `/opt/subtrack/app.env` 환경변수로 운영 값을 주입한다.

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

`database/00_rds_init_all.sql`은 신규 RDS의 빈 `subtrack` DB를 위한 통합 초기화 파일이다.

`database/03_*` 이후 파일은 기존 로컬 DB를 유지하면서 변경분을 반영하기 위한 migration이다. 신규 RDS에 `00_rds_init_all.sql`을 적용한 뒤 `03~06`을 이어서 적용하지 않는다. 특히 `03_add_billing_start_date.sql`은 이미 존재하는 컬럼/인덱스와 충돌할 수 있다.

JDBC URL 예시 형식:

```env
DB_URL=jdbc:mysql://<rds-endpoint>:3306/subtrack?serverTimezone=Asia/Seoul&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci&characterSetResults=utf8mb4
```

`<rds-endpoint>`, 사용자명, 비밀번호는 코드나 문서에 실제 값으로 저장하지 않는다.

## 3. RDS 보안 그룹 개요

RDS 보안 그룹은 EC2 백엔드에서만 접근할 수 있게 제한한다.

Inbound 예시:

```txt
Type  : MySQL/Aurora
Port  : 3306
Source: <ec2-security-group-id>
```

주의사항:

- RDS를 `0.0.0.0/0`에 공개하지 않는다.
- 로컬 PC에서 직접 RDS에 접속해야 하는 임시 상황이면, 작업자 IP만 짧게 허용하고 작업 후 제거한다.
- EC2와 RDS가 같은 VPC 안에 있어야 보안 그룹 참조 방식으로 접근 제어하기 쉽다.
- 운영 DB 계정은 root 계정 대신 애플리케이션 전용 계정을 사용하는 방향을 권장한다.

## 4. EC2 생성 개요

EC2는 Spring Boot JAR를 실행하는 백엔드 서버로 사용한다.

권장 시작 설정:

```txt
Region       : ap-northeast-2
OS           : Amazon Linux 2023 또는 Ubuntu LTS
Runtime      : Java 17
App path     : /opt/subtrack
Service name : subtrack-backend
```

EC2 보안 그룹 Inbound 예시:

```txt
SSH 22       : <admin-ip>/32
Backend 8080: 테스트용으로만 제한 허용
```

운영 프론트엔드가 CloudFront HTTPS로 서비스되면, 브라우저의 mixed content 정책 때문에 HTTP 백엔드 호출이 막힐 수 있다. 실제 운영 연결에서는 `VITE_BACKSERVER`에 HTTPS 백엔드 API 주소를 넣는 구성을 권장한다.

## 5. EC2에서 Spring Boot JAR 실행 개요

백엔드는 Maven Wrapper로 빌드한다.

로컬 Windows 빌드 예시:

```powershell
cd backend
.\mvnw.cmd clean package
```

EC2 Linux 빌드 예시:

```bash
cd backend
./mvnw clean package
```

생성된 JAR는 EC2의 앱 디렉터리에 배치한다.

```txt
/opt/subtrack/subtrack-backend.jar
/opt/subtrack/app.env
```

`/opt/subtrack/app.env`에는 운영 환경변수를 둔다. 실제 값은 `docs/aws-secrets-and-env.md`의 placeholder를 기준으로 별도 관리한다.

systemd unit 예시:

```ini
[Unit]
Description=SubTrack Spring Boot Backend
After=network.target

[Service]
User=<ec2-app-user>
WorkingDirectory=/opt/subtrack
EnvironmentFile=/opt/subtrack/app.env
ExecStart=/usr/bin/java -jar /opt/subtrack/subtrack-backend.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

운영 확인 명령 예시:

```bash
sudo systemctl daemon-reload
sudo systemctl enable subtrack-backend
sudo systemctl start subtrack-backend
sudo systemctl status subtrack-backend
sudo journalctl -u subtrack-backend -f
```

헬스 체크:

```txt
GET /api/health
```

Swagger UI:

```txt
GET /swagger-ui.html
```

## 6. S3 + CloudFront frontend 배포 개요

프론트엔드는 Vite build 결과물인 `frontend/dist`를 S3에 업로드하고 CloudFront로 제공한다.

빌드 예시:

```bash
cd frontend
npm ci
npm run build
```

S3 구성 개요:

```txt
Bucket public access : Block all public access
Object source        : frontend/dist
Static website hosting: CloudFront OAC를 사용할 경우 비활성화 가능
```

CloudFront 구성 개요:

```txt
Origin              : S3 bucket
Default root object : index.html
Viewer protocol     : Redirect HTTP to HTTPS
SPA fallback        : 403/404 -> /index.html, status 200
```

React Router를 사용하므로 `/dashboard`, `/subscriptions` 같은 직접 접근 경로가 새로고침되어도 깨지지 않도록 CloudFront custom error response를 설정한다.

## 7. CloudFront OAC 사용 권장

S3 버킷은 public으로 열지 않고 CloudFront Origin Access Control(OAC)을 통해서만 읽히게 구성하는 것을 권장한다.

권장 방향:

```txt
1. S3 Block Public Access 유지
2. CloudFront Origin Access Control 생성
3. CloudFront distribution에 OAC 연결
4. S3 bucket policy는 해당 CloudFront distribution만 허용
```

S3 bucket policy에는 실제 AWS ARN을 문서에 기록하지 않고 placeholder만 사용한다.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "cloudfront.amazonaws.com"
      },
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::<s3-bucket-name>/*",
      "Condition": {
        "StringEquals": {
          "AWS:SourceArn": "<cloudfront-distribution-arn>"
        }
      }
    }
  ]
}
```

## 8. GitHub Actions 연결 개요

GitHub Actions는 프론트엔드와 백엔드 배포 자동화에 사용한다.

프론트엔드 배포 흐름:

```txt
1. checkout
2. setup node
3. frontend npm ci
4. VITE_BACKSERVER 주입
5. npm run build
6. dist 파일 S3 sync
7. CloudFront invalidation
```

백엔드 배포 흐름:

```txt
1. checkout
2. setup Java 17
3. backend Maven package
4. JAR를 EC2 /opt/subtrack로 업로드
5. systemd service restart
6. /api/health 확인
```

AWS 인증은 장기 access key보다 GitHub OIDC와 IAM Role 방식을 우선 검토한다. SSH 방식으로 EC2에 배포한다면 private key는 GitHub Secrets에 저장하고, EC2 접속 host/user 정보는 `docs/aws-secrets-and-env.md` 기준으로 관리한다.

이 작업에서는 workflow 파일을 만들지 않는다. 실제 CI/CD workflow는 별도 작업에서 작성한다.

## 9. 배포 후 확인 순서

배포 후에는 아래 순서로 확인한다.

```txt
1. RDS가 private 상태인지 확인
2. RDS 보안 그룹이 EC2 보안 그룹만 3306으로 허용하는지 확인
3. RDS에 database/00_rds_init_all.sql 적용 여부 확인
4. EC2 /opt/subtrack/app.env에 운영 환경변수가 있는지 확인
5. EC2 systemd service가 active 상태인지 확인
6. 백엔드 /api/health 응답 확인
7. Swagger UI 접근 필요 시 /swagger-ui.html 확인
8. frontend npm run build 성공 확인
9. frontend/dist가 S3에 업로드되었는지 확인
10. CloudFront invalidation 후 프론트엔드 접속 확인
11. 회원가입, 로그인, 구독 등록, 목록 조회, 대시보드 조회 확인
12. 브라우저 콘솔에서 CORS 또는 mixed content 오류가 없는지 확인
13. EC2 로그와 애플리케이션 로그에서 DB 연결 오류가 없는지 확인
```

## 10. Backend health check와 CORS 확인

현재 백엔드 health check endpoint는 아래와 같다.

```txt
GET /api/health
```

이 endpoint는 인증 없이 접근 가능해야 하며, DB 연결 상태까지 확인하지 않는 애플리케이션 기동 확인용 endpoint다.

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

## 12. RDS 신규 DB 초기화 SQL

신규 RDS MySQL은 DB name을 `subtrack`으로 만드는 것을 권장한다.

권장 DB 설정:

```txt
Engine    : MySQL 8.x
DB name   : subtrack
Charset   : utf8mb4
Collation : utf8mb4_unicode_ci
```

RDS 보안 그룹은 EC2 security group에서만 MySQL 3306 접근을 허용한다.

```txt
Type  : MySQL/Aurora
Port  : 3306
Source: <ec2-security-group-id>
```

신규 RDS의 빈 `subtrack` DB에는 아래 파일을 사용한다.

```txt
database/00_rds_init_all.sql
```

적용 예시:

```bash
mysql -h <rds-endpoint> -P 3306 -u <rds-app-username> -p subtrack < database/00_rds_init_all.sql
```

비밀번호는 명령어에 직접 쓰지 말고 `-p` 프롬프트에서 입력한다.

`database/03_add_billing_start_date.sql`부터 `database/06_add_exchange_rate.sql`까지는 기존 로컬 DB를 보정하던 migration이다. 신규 RDS에 `00_rds_init_all.sql`을 적용했다면 `03~06`을 다시 적용하지 않는다.

SQL 적용 후 확인 쿼리:

```sql
SHOW TABLES;
SELECT COUNT(*) FROM subscription_category;
SELECT COUNT(*) FROM exchange_rate;
SELECT COUNT(*) FROM subscription_status_history;
```

`subscription_category`는 10건, `exchange_rate`는 SYSTEM fallback seed 4건이면 정상이다. 신규 DB라면 `subscription_status_history`는 0건이어도 정상이다.

프로젝트의 실제 카테고리 테이블명은 `category`가 아니라 `subscription_category`다.

운영 CORS origin은 EC2 환경변수 또는 GitHub Actions 배포 과정에서 아래 값으로 주입한다.

```env
CORS_ALLOWED_ORIGINS=https://<frontend-domain>
```

여러 origin이 필요하면 쉼표로 구분한다.

```env
CORS_ALLOWED_ORIGINS=http://localhost:5173,https://<cloudfront-domain>
```

`*` origin은 허용하지 않는다. CloudFront 배포 후에는 실제 프론트엔드 origin이 `CORS_ALLOWED_ORIGINS`에 포함되어 있는지 먼저 확인한다.

## 11. EC2 backend JAR + systemd 배포 템플릿

백엔드 운영 프로필 설정은 아래 파일에 둔다.

```txt
backend/src/main/resources/application-prod.yml
```

이 파일은 `SPRING_PROFILES_ACTIVE=prod`일 때 사용되며, 로컬 기본 실행용 `application.yml`은 그대로 유지한다. 운영에서 민감하거나 환경마다 달라지는 값은 EC2의 `/opt/subtrack/app.env`로 주입한다.

운영 환경변수 예시는 아래 파일에서 확인한다.

```txt
deploy/backend/app.env.example
```

EC2에서는 예시 파일을 참고해 실제 파일을 만든다.

```bash
sudo mkdir -p /opt/subtrack
sudo cp deploy/backend/app.env.example /opt/subtrack/app.env
sudo vi /opt/subtrack/app.env
sudo chmod 600 /opt/subtrack/app.env
```

`/opt/subtrack/app.env`에는 실제 운영 값이 들어가므로 Git에 커밋하지 않는다. 실제 RDS endpoint, DB password, JWT secret, CloudFront domain은 문서에 기록하지 않고 EC2 환경 또는 GitHub Secrets에서만 관리한다.

systemd 서비스 템플릿은 아래 파일에 둔다.

```txt
deploy/backend/subtrack.service
```

EC2에서는 `<ec2-app-user>`를 실제 실행 사용자로 바꾼 뒤 systemd 경로에 복사한다.

```bash
sudo cp deploy/backend/subtrack.service /etc/systemd/system/subtrack-backend.service
sudo vi /etc/systemd/system/subtrack-backend.service
sudo systemctl daemon-reload
```

JAR 배치와 서비스 재시작은 아래 스크립트 템플릿을 사용한다.

```txt
deploy/backend/deploy-backend.sh
```

사용 예시:

```bash
chmod +x deploy/backend/deploy-backend.sh
./deploy/backend/deploy-backend.sh ./subtrack-backend.jar
```

스크립트는 `/opt/subtrack` 디렉터리를 만들고, 전달받은 JAR를 `/opt/subtrack/subtrack-backend.jar`로 복사한 뒤 `subtrack-backend` systemd 서비스를 enable/restart한다.

배포 후 서비스 상태와 로그는 아래 명령으로 확인한다.

```bash
sudo systemctl status subtrack-backend --no-pager
sudo journalctl -u subtrack-backend -f
```

애플리케이션 기동 확인은 health check endpoint로 한다.

```bash
curl http://localhost:8080/api/health
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
