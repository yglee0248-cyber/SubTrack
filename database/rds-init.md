# SubTrack RDS MySQL Initialization

이 문서는 새 RDS MySQL에 SubTrack 운영 DB를 처음 준비할 때 사용하는 SQL 파일과 실행 흐름을 정리한다.

실제 RDS endpoint, DB 비밀번호, AWS ARN, access key, secret key, JWT secret은 이 문서에 기록하지 않는다.

## 1. SQL 파일 역할

| 파일 | 용도 |
|---|---|
| `00_rds_init_all.sql` | 신규 RDS의 빈 `subtrack` DB를 한 번에 초기화하는 통합 SQL |
| `01_schema.sql` | 현재까지 정리된 전체 스키마 기준 파일 |
| `02_seed_category.sql` | 기본 구독 카테고리 seed |
| `03_add_billing_start_date.sql` | 기존 로컬 DB에 `billing_start_date`를 추가하던 migration |
| `04_add_subscription_status_history.sql` | 기존 구독 데이터에 상태 이력 테이블과 초기 이력을 보정하던 migration |
| `05_update_category_names_ko.sql` | 기존 DB의 깨진 카테고리명을 한국어로 보정하던 migration |
| `06_add_exchange_rate.sql` | 기존 DB에 `exchange_rate` 테이블과 SYSTEM fallback seed를 추가하던 migration |

신규 RDS에는 `00_rds_init_all.sql`을 우선 사용한다.

## 2. 로컬 DB 초기화와 RDS 신규 DB 초기화 차이

로컬 개발 DB는 Docker MySQL과 기존 migration 파일을 이용해 단계적으로 보정할 수 있다.

신규 RDS는 빈 DB에서 시작하므로, 현재 애플리케이션이 필요한 테이블과 초기 데이터를 한 번에 준비하는 `00_rds_init_all.sql`을 사용한다.

`00_rds_init_all.sql`은 아래를 포함한다.

- `member`
- `subscription_category`
- `subscription`
- `subscription_status_history`
- `exchange_rate`
- 기본 한국어 카테고리 10개
- `exchange_rate` SYSTEM fallback seed 4개

현재 backend 코드가 사용하지 않는 P1 후보 테이블인 `payment_history`, `notification`, `push_log`는 신규 RDS 통합 초기화 파일에 포함하지 않는다.

## 3. 03~06 migration 적용 판단

새 RDS에 `00_rds_init_all.sql`을 적용했다면 `03~06` migration을 이어서 적용하지 않는다.

이유:

- `03_add_billing_start_date.sql`: `subscription.billing_start_date`와 인덱스가 이미 있으므로 중복 컬럼 또는 중복 인덱스 충돌이 날 수 있다.
- `04_add_subscription_status_history.sql`: 테이블은 이미 있고, 기존 subscription 데이터를 상태 이력으로 보정하는 목적이다. 신규 빈 RDS에는 보정할 기존 데이터가 없다.
- `05_update_category_names_ko.sql`: 기존 DB의 깨진 카테고리명을 보정하는 목적이다. `00_rds_init_all.sql`은 처음부터 최종 한국어 카테고리명을 넣는다.
- `06_add_exchange_rate.sql`: `exchange_rate` 테이블과 SYSTEM fallback seed가 이미 `00_rds_init_all.sql`에 포함되어 있다.

기존 로컬 DB를 유지하면서 변경분만 반영할 때는 `03~06` migration을 상황에 맞게 사용할 수 있다.

## 4. RDS 적용 전제

RDS 생성 시 DB name은 아래처럼 준비하는 것을 권장한다.

```txt
subtrack
```

문자셋과 collation은 아래 기준을 권장한다.

```txt
charset   : utf8mb4
collation : utf8mb4_unicode_ci
```

`00_rds_init_all.sql`은 `CREATE DATABASE`와 `USE subtrack`을 포함하지 않는다. RDS에서 `subtrack` DB에 접속한 뒤 실행한다.

## 5. RDS에 SQL 적용 예시

Linux/macOS/Git Bash 예시:

```bash
mysql -h <rds-endpoint> -P 3306 -u <rds-app-username> -p subtrack < database/00_rds_init_all.sql
```

PowerShell 예시:

```powershell
cmd /c "type database\00_rds_init_all.sql | mysql -h <rds-endpoint> -P 3306 -u <rds-app-username> -p subtrack"
```

비밀번호는 명령어에 직접 쓰지 말고 `-p` 입력 프롬프트에서 입력한다.

## 6. 적용 후 확인 쿼리

```sql
SHOW TABLES;
SELECT COUNT(*) FROM subscription_category;
SELECT COUNT(*) FROM exchange_rate;
SELECT COUNT(*) FROM subscription_status_history;
```

정상 기준:

- `subscription_category`는 기본 seed 10건이어야 한다.
- `exchange_rate`는 SYSTEM fallback seed 4건이어야 한다.
- `subscription_status_history`는 신규 DB라면 0건이어도 정상이다.

프로젝트의 실제 카테고리 테이블명은 `category`가 아니라 `subscription_category`다.

## 7. 로컬 Docker MySQL에서 안전하게 검증하는 방법

기존 로컬 `subtrack` DB를 drop하거나 초기화하지 않는다.

필요하면 새 임시 Docker MySQL 컨테이너를 만들어 검증한다. 아래는 문서용 예시이며, 실제 실행 전 포트와 컨테이너 이름이 겹치지 않는지 확인한다.

```bash
docker run --name subtrack-rds-init-test \
  -e MYSQL_ROOT_PASSWORD=<test-password> \
  -e MYSQL_DATABASE=subtrack \
  -p 3310:3306 \
  -d mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

적용 예시:

```bash
mysql -h 127.0.0.1 -P 3310 -u root -p subtrack < database/00_rds_init_all.sql
```

확인 예시:

```bash
mysql -h 127.0.0.1 -P 3310 -u root -p subtrack
```

```sql
SHOW TABLES;
SELECT COUNT(*) FROM subscription_category;
SELECT COUNT(*) FROM exchange_rate;
```

검증이 끝난 임시 컨테이너 정리 예시:

```bash
docker rm -f subtrack-rds-init-test
```
