# AWS Cleanup Checklist

## 1. 목적

이 문서는 SubTrack AWS 배포 후 비용이 계속 발생하지 않도록 리소스를 정리하기 위한 체크리스트다.

AWS 리소스를 생성한 뒤 프로젝트가 종료되었거나, 취업 후 더 이상 운영하지 않을 경우 이 문서를 기준으로 삭제한다.

주의사항:

```txt
- EC2를 stop만 해도 EBS, Elastic IP 등에서 비용이 남을 수 있다.
- RDS를 stop만 하면 장기적으로 다시 시작될 수 있으므로 삭제 여부를 확인해야 한다.
- S3 bucket은 객체를 먼저 비워야 삭제할 수 있다.
- CloudFront는 Disable 후 Delete해야 한다.
- Route 53 Hosted Zone은 남아 있으면 비용이 발생할 수 있다.
- 계정을 닫아도 이미 발생한 비용은 청구될 수 있다.
```

## 2. 삭제 전 준비

삭제 전 확인한다.

```txt
- 필요한 데이터 백업 여부 확인
- 최종 포트폴리오 캡처 또는 시연 영상 확보
- README의 배포 URL 제거 또는 비활성화 표시
- RDS 데이터가 더 이상 필요 없는지 확인
- 가족 명의 계정인 경우 계정 명의자에게 삭제 진행 공유
```

## 3. Frontend 삭제

### CloudFront

```txt
- [ ] CloudFront Distribution 확인
- [ ] Distribution Disable
- [ ] Deployed 상태까지 대기
- [ ] Distribution Delete
```

CloudFront는 Disable 직후 바로 삭제되지 않을 수 있다.
상태가 반영될 때까지 기다린 뒤 삭제한다.

### S3

```txt
- [ ] frontend 배포 bucket 확인
- [ ] bucket 안의 모든 객체 삭제
- [ ] versioning이 켜져 있다면 object version/delete marker까지 삭제
- [ ] bucket 삭제
- [ ] log bucket을 따로 만들었다면 log bucket도 삭제
```

## 4. Backend 삭제

### EC2

```txt
- [ ] EC2 instance 중지
- [ ] EC2 instance terminate
- [ ] 연결된 EBS volume 삭제 여부 확인
- [ ] 사용하지 않는 EBS volume 삭제
- [ ] EBS snapshot 삭제
- [ ] Elastic IP 사용 여부 확인
- [ ] Elastic IP release
- [ ] Key Pair 삭제
- [ ] 사용하지 않는 Security Group 삭제
```

주의:

```txt
EC2 instance를 stop만 하면 완전 삭제가 아니다.
EBS volume, snapshot, Elastic IP는 별도 비용이 남을 수 있다.
```

### Backend Docker 사용 시

EC2 안에서 Docker로 실행했다면 EC2 삭제 전에 확인한다.

```txt
- [ ] backend container stop
- [ ] backend container remove
- [ ] backend image remove
- [ ] 사용하지 않는 Docker volume 확인
```

EC2 자체를 terminate하면 내부 Docker 리소스도 같이 사라지지만, 백업이 필요하면 terminate 전 확인한다.

## 5. Database 삭제

### RDS

```txt
- [ ] RDS DB instance 확인
- [ ] 애플리케이션 연결 종료
- [ ] DB instance delete
- [ ] Final snapshot 생성 여부 선택
- [ ] 프로젝트 종료 시 final snapshot을 만들지 않거나, 만든 뒤 필요 없어지면 삭제
- [ ] Manual snapshot 삭제
- [ ] Automated backup 삭제 여부 확인
- [ ] DB subnet group 삭제
- [ ] Custom parameter group 삭제
- [ ] Custom option group 삭제
```

주의:

```txt
RDS snapshot이 남아 있으면 DB instance를 삭제해도 비용이 남을 수 있다.
```

## 6. Network 리소스 삭제

초기 구성에서는 NAT Gateway와 ALB를 사용하지 않는 것을 원칙으로 한다.
만약 실수로 만들었거나 추후 사용했다면 반드시 확인한다.

```txt
- [ ] NAT Gateway 삭제
- [ ] Load Balancer 삭제
- [ ] Target Group 삭제
- [ ] Elastic IP release
- [ ] 사용하지 않는 Security Group 삭제
- [ ] 사용하지 않는 VPC Endpoint 삭제
```

주의:

```txt
NAT Gateway와 ALB는 개인 포트폴리오에서 비용 부담이 커질 수 있으므로 사용 여부를 특히 확인한다.
```

## 7. Domain / DNS / Certificate 삭제

### Route 53

```txt
- [ ] Hosted Zone 확인
- [ ] DNS Record 삭제
- [ ] Hosted Zone 삭제
- [ ] 등록한 Domain 자동 갱신 여부 확인
- [ ] 더 이상 사용하지 않는 Domain은 auto-renew 비활성화
```

주의:

```txt
Hosted Zone은 서비스가 내려가도 남아 있으면 비용이 발생할 수 있다.
Domain 등록 비용은 일반적으로 즉시 회수되지 않을 수 있다.
```

### ACM

```txt
- [ ] 사용하지 않는 SSL certificate 확인
- [ ] CloudFront 또는 ALB 연결 해제 확인
- [ ] ACM certificate 삭제
```

## 8. CloudWatch 삭제

```txt
- [ ] CloudWatch Log Group 확인
- [ ] backend 관련 Log Group 삭제
- [ ] RDS 관련 Log Group 삭제
- [ ] CloudFront log 저장 여부 확인
- [ ] CloudWatch Alarm 삭제
- [ ] Dashboard를 만들었다면 Dashboard 삭제
```

주의:

```txt
로그가 장기간 쌓이면 비용이 발생할 수 있으므로 보관 기간도 확인한다.
```

## 9. IAM 정리

```txt
- [ ] 배포용 IAM User 확인
- [ ] Access Key 비활성화
- [ ] Access Key 삭제
- [ ] 사용하지 않는 IAM Role 삭제
- [ ] 직접 만든 IAM Policy 삭제
- [ ] 불필요한 권한 제거
```

주의:

```txt
Root 계정은 삭제 대상이 아니다.
계정 자체를 닫기 전까지 Root 계정 MFA는 유지한다.
```

## 10. Secrets / 환경변수 정리

```txt
- [ ] EC2에 저장된 .env 파일 삭제
- [ ] GitHub Actions Secrets를 사용했다면 삭제
- [ ] 로컬에 저장한 AWS access key 삭제
- [ ] Notion/README 등에 노출된 임시 URL 또는 secret 확인
```

절대 GitHub에 올라가면 안 되는 정보:

```txt
- DB password
- JWT secret
- AWS access key
- OneSignal REST API key
- RDS endpoint와 계정 정보 조합
```

## 11. Billing 확인

리소스 삭제 후 반드시 Billing에서 확인한다.

```txt
- [ ] Billing > Bills 확인
- [ ] Charges by service 펼쳐서 확인
- [ ] Charges by region 확인
- [ ] Cost Explorer 확인
- [ ] Free Tier 사용량 확인
- [ ] 예상 비용이 남아 있는지 확인
```

확인해야 할 서비스 예시:

```txt
- EC2
- EBS
- RDS
- S3
- CloudFront
- Route 53
- CloudWatch
- VPC
```

## 12. 계정 종료 전 확인

계정 자체를 닫을 경우 아래를 확인한다.

```txt
- [ ] 모든 리소스 삭제 완료
- [ ] Billing에서 남은 비용 확인
- [ ] 다음 달 청구 가능성 확인
- [ ] 가족 명의 계정이면 명의자에게 계정 종료 여부 확인
- [ ] 필요한 백업 파일 로컬 저장 완료
```

주의:

```txt
AWS 계정을 닫아도 이미 발생한 비용이 사라지는 것은 아니다.
계정 종료 전 사용한 리소스 비용은 청구될 수 있다.
```

## 13. 최종 체크리스트

전체 삭제 후 아래 항목을 모두 확인한다.

```txt
Frontend
- [ ] CloudFront deleted
- [ ] S3 frontend bucket deleted
- [ ] S3 log bucket deleted if created

Backend
- [ ] EC2 instance terminated
- [ ] EBS volumes deleted
- [ ] EBS snapshots deleted
- [ ] Elastic IP released
- [ ] Key pair deleted

Database
- [ ] RDS instance deleted
- [ ] RDS snapshots deleted
- [ ] RDS subnet group deleted
- [ ] RDS parameter group deleted if custom

Network
- [ ] NAT Gateway deleted if created
- [ ] Load Balancer deleted if created
- [ ] Target Group deleted if created
- [ ] Unused Security Groups deleted

Domain / HTTPS
- [ ] Route 53 Hosted Zone deleted if unused
- [ ] Domain auto-renew disabled if unused
- [ ] ACM certificate deleted if unused

Logs / Monitoring
- [ ] CloudWatch Log Groups deleted
- [ ] CloudWatch Alarms deleted

IAM / Secrets
- [ ] Access keys deleted
- [ ] Unused IAM users deleted
- [ ] Unused IAM roles deleted
- [ ] GitHub Secrets removed if used
- [ ] Local .env files removed from server

Billing
- [ ] Bills checked
- [ ] Cost Explorer checked
- [ ] No active paid resources remain
```

## 14. 포트폴리오 설명 포인트

이 문서를 통해 설명할 수 있는 내용은 다음과 같다.

```txt
- AWS 배포 시 비용 발생 가능성을 고려해 리소스 삭제 문서를 별도로 작성했습니다.
- EC2, RDS, S3, CloudFront, Route 53 등 주요 리소스별 삭제 절차를 정리했습니다.
- 단순 구현뿐 아니라 운영 종료와 비용 관리까지 고려했습니다.
```
