# AWS Deploy Plan

## 1. 배포 목표

SubTrack은 로컬 개발 환경에서 기능을 완성한 뒤, AWS에 배포 가능한 구조로 운영한다.

배포 목표는 다음과 같다.

- 프론트엔드는 정적 파일로 빌드해 배포한다.
- 백엔드는 Spring Boot 애플리케이션으로 실행한다.
- 데이터베이스는 MySQL 기준으로 운영한다.
- 로컬 개발 DB와 배포 DB를 환경변수로 분리한다.
- AWS 비용이 과도하게 발생하지 않도록 최소 구성으로 시작한다.
- 추후 서비스 종료 또는 취업 후 비용 방지를 위해 삭제 절차를 문서화한다.

## 2. 기본 배포 구성

SubTrack의 AWS 배포 기본 구성은 다음과 같다.

```txt
Frontend : S3 + CloudFront
Backend  : EC2
Database : RDS MySQL
HTTPS    : ACM
Domain   : Route 53 선택
```

초기 포트폴리오 배포에서는 비용 절감을 위해 아래 리소스는 사용하지 않는다.

```txt
- NAT Gateway
- ALB
- Auto Scaling
- Multi-AZ RDS
- 고성능 인스턴스
- 불필요한 Elastic IP 여러 개
```

## 3. 로컬 개발 환경

로컬 개발은 AWS가 아니라 Docker MySQL 기준으로 진행한다.

```txt
Frontend : localhost:5173
Backend  : localhost:8080
Database : Docker MySQL
```

로컬 DB 접속 예시는 다음과 같다.

```env
DB_URL=jdbc:mysql://localhost:3307/subtrack?serverTimezone=Asia/Seoul&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci&characterSetResults=utf8mb4
DB_USERNAME=root
DB_PASSWORD=password
```

PC의 3306 포트가 이미 사용 중일 수 있으므로, 로컬 Docker MySQL은 기본적으로 다음 포트 매핑을 사용한다.

```txt
localhost:3307 -> container:3306
```

## 4. 환경변수 관리

배포 환경에서는 DB 접속 정보와 보안 정보를 코드에 직접 작성하지 않는다.

필수 환경변수는 다음과 같다.

```env
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
CORS_ALLOWED_ORIGINS=
EXCHANGE_RATE_BASE_URL=
```

로컬 예시:

```env
DB_URL=jdbc:mysql://localhost:3307/subtrack?serverTimezone=Asia/Seoul&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci&characterSetResults=utf8mb4
DB_USERNAME=root
DB_PASSWORD=password
JWT_SECRET=local-development-secret-key-change-later
CORS_ALLOWED_ORIGINS=http://localhost:5173
EXCHANGE_RATE_BASE_URL=https://api.frankfurter.dev
```

AWS RDS 예시:

```env
DB_URL=jdbc:mysql://{rds-endpoint}:3306/subtrack?serverTimezone=Asia/Seoul&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci&characterSetResults=utf8mb4
DB_USERNAME=admin
DB_PASSWORD={rds-password}
JWT_SECRET={production-jwt-secret}
CORS_ALLOWED_ORIGINS=https://{cloudfront-domain}
```

## 5. Backend 배포 계획

백엔드는 EC2에서 Spring Boot JAR 또는 Docker 컨테이너로 실행한다.

초기 배포 방식은 다음 중 하나를 선택한다.

### 선택 A. JAR 직접 실행

```txt
1. 로컬 또는 EC2에서 Maven package 실행
2. target/*.jar 파일 생성
3. EC2에 JAR 업로드
4. 환경변수 설정
5. java -jar 명령으로 실행
```

예시:

```bash
java -jar subtrack-backend.jar
```

### 선택 B. Docker 실행

```txt
1. backend/Dockerfile 작성
2. Docker image build
3. EC2에서 container 실행
4. 환경변수 주입
```

예시:

```bash
docker run -d \
  --name subtrack-backend \
  -p 8080:8080 \
  -e DB_URL="${DB_URL}" \
  -e DB_USERNAME="${DB_USERNAME}" \
  -e DB_PASSWORD="${DB_PASSWORD}" \
  -e JWT_SECRET="${JWT_SECRET}" \
  subtrack-backend
```

포트폴리오 설명 관점에서는 Docker 실행 방식이 더 명확하지만, 초기에는 JAR 직접 실행도 가능하다.

## 6. Frontend 배포 계획

프론트엔드는 Vite 빌드 결과물을 S3에 업로드하고 CloudFront로 배포한다.

```txt
1. frontend에서 npm install
2. npm run build
3. dist/ 생성
4. S3 bucket에 dist 파일 업로드
5. CloudFront distribution 생성
6. CloudFront domain으로 접속 확인
```

프론트엔드 환경변수 예시:

```env
VITE_BACKSERVER=https://{backend-domain-or-ec2-public-url}
```

초기에는 EC2 public IP 또는 EC2 도메인을 사용할 수 있다.
추후 도메인을 연결하면 `api.example.com` 형태로 변경할 수 있다.

## 7. Database 배포 계획

DB는 RDS MySQL을 사용한다.

기본 원칙은 다음과 같다.

```txt
- DB engine: MySQL
- DB name: subtrack
- Public access는 가능하면 제한
- 백엔드 EC2에서만 접근 가능하도록 Security Group 설정
- Multi-AZ는 초기에는 사용하지 않음
- 스토리지는 최소 용량으로 시작
```

초기 schema 적용 순서:

```txt
1. database/01_schema.sql 실행
2. database/02_seed_category.sql 실행
3. subscription_category seed 10개 확인
```

확인 SQL:

```sql
SHOW TABLES;
SELECT COUNT(*) FROM subscription_category;
```

## 8. Security Group 계획

### EC2 Security Group

허용할 인바운드:

```txt
SSH 22      : 내 IP만 허용
HTTP 8080   : 프론트 또는 테스트용 접근 허용
```

초기 테스트에서는 8080을 열 수 있지만, 최종적으로는 Nginx/HTTPS 구성을 검토한다.

### RDS Security Group

허용할 인바운드:

```txt
MySQL 3306 : EC2 Security Group에서만 허용
```

RDS를 전체 공개하지 않는다.

## 9. CORS 계획

로컬 개발:

```txt
http://localhost:5173
```

AWS 배포:

```txt
https://{cloudfront-domain}
https://{custom-domain}
```

백엔드는 `CORS_ALLOWED_ORIGINS` 환경변수로 허용 Origin을 관리한다.

## 10. 배포 전 확인 목록

배포 전 확인할 항목은 다음과 같다.

```txt
- Maven package 성공
- 테스트용 Docker MySQL에서 schema 적용 성공
- 회원가입 / 로그인 / JWT 인증 성공
- 구독 CRUD 성공
- 대시보드 API 성공
- Swagger 접속 가능
- 프론트엔드 API base URL 환경변수 분리
- application.yml에 민감정보 직접 작성 없음
- .env 파일이 git에 올라가지 않음
```

## 11. 배포 후 확인 목록

배포 후 확인할 항목은 다음과 같다.

```txt
- 프론트엔드 URL 접속 가능
- 백엔드 health check 응답 정상
- Swagger 접속 가능
- 회원가입 가능
- 로그인 가능
- 구독 등록 가능
- 구독 목록 조회 가능
- 대시보드 조회 가능
- DB에 데이터 정상 저장
- 브라우저 콘솔 CORS 오류 없음
```

## 12. 비용 관리 원칙

AWS 계정 생성 직후 다음 설정을 먼저 진행한다.

```txt
- Root 계정 MFA 설정
- Admin IAM 계정 MFA 설정
- AWS Budgets 비용 알림 설정
- Free Tier 사용량 알림 설정
```

초기 포트폴리오 배포에서는 다음을 사용하지 않는다.

```txt
- NAT Gateway
- ALB
- Multi-AZ RDS
- 고성능 EC2/RDS 인스턴스
- 불필요한 Elastic IP
- 불필요한 CloudWatch 로그 장기 보관
```

## 13. 포트폴리오 설명 포인트

면접에서 설명할 수 있는 배포 포인트는 다음과 같다.

```txt
- 프론트엔드는 정적 빌드 후 S3/CloudFront로 배포할 수 있도록 설계했습니다.
- 백엔드는 EC2에서 Spring Boot 애플리케이션으로 실행할 수 있도록 환경변수를 분리했습니다.
- DB는 로컬 Docker MySQL과 AWS RDS MySQL을 같은 DDL 기준으로 사용할 수 있게 설계했습니다.
- JWT secret, DB 접속 정보, CORS origin은 코드가 아니라 환경변수로 관리했습니다.
- AWS 비용 발생을 고려해 cleanup checklist를 별도 문서로 관리했습니다.
```
