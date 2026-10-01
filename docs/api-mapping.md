# 관리자 화면–API–ERD 연결 초안

Swagger/OpenAPI를 작성할 때 이 표를 기준으로 화면 기능과 데이터 테이블을 연결합니다. 실제 URI와 요청·응답 스키마는 백엔드 구현 과정에서 확정합니다.

| 관리자 기능 | 예정 API | 주요 ERD 테이블 |
|---|---|---|
| 로그인 | `POST /api/admin/auth/login` | `users`, `auth_identities`, `sessions` |
| 로그아웃 | `POST /api/admin/auth/logout` | `sessions` |
| 대시보드 업무 건수 | `GET /api/admin/dashboard/tasks` | `events`, `reports` |
| 서비스 이용 통계 | `GET /api/admin/statistics` | `users`, `events`, `participations` 및 집계 데이터 |
| 이벤트 심사 목록 | `GET /api/admin/events/reviews` | `events`, `event_versions`, `users`, `profiles` |
| 이벤트 심사 상세 | `GET /api/admin/events/{eventId}` | `events`, `event_versions`, `users`, `profiles` |
| 심사 시작 | `POST /api/admin/events/{eventId}/review/start` | `events`, `moderation_actions` |
| 이벤트 승인 | `POST /api/admin/events/{eventId}/review/approve` | `events`, `moderation_actions` |
| 이벤트 불가 판정 | `POST /api/admin/events/{eventId}/review/reject` | `events`, `moderation_actions` |
| 이벤트 운영 목록 | `GET /api/admin/events` | `events`, `event_versions` |
| 참가자 조회 | `GET /api/admin/events/{eventId}/participants` | `participations`, `users`, `profiles` |
| 신고 목록 | `GET /api/admin/reports` | `reports`, `users`, `events` |
| 신고 상세 | `GET /api/admin/reports/{reportId}` | `reports`, `users`, `events` |
| 신고 검토 시작 | `POST /api/admin/reports/{reportId}/review/start` | `reports`, `audit_logs` |
| 신고 처리 완료 | `POST /api/admin/reports/{reportId}/resolve` | `reports`, `event_dispositions`, `audit_logs` |

## 상태 전이

```text
이벤트 심사
pending → reviewing → approved
                    ↘ rejected

이벤트 운영
not_open → open → ended
                 ↘ cancelled
                 ↘ suspended

신고 처리
received → investigating → resolved
```

## 검수 시 함께 제공할 자료

1. Swagger UI 주소
2. `/v3/api-docs` OpenAPI JSON
3. `earthuu_erd.png`
4. 이 화면–API–ERD 연결표
5. 테스트 서버 주소와 관리자 테스트 계정

