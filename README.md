# Earthuu 관리자 시스템

IA 및 ERD를 기준으로 구현하는 Earthuu 관리자 시스템입니다. 현재 프론트엔드는 정적 시제품이며, 백엔드는 Java 21과 Spring Boot로 구성할 예정입니다.

## 프로젝트 구조

```text
earthuu-admin/
├── frontend/                  # 관리자 정적 프론트엔드
│   ├── index.html
│   ├── app.js
│   ├── style.css
│   └── README.md
├── backend/                   # Java 21 + Spring Boot API
│   ├── src/main/java/com/earthuu/admin/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   ├── src/main/resources/db/migration/
│   ├── src/test/java/com/earthuu/admin/
│   └── README.md
├── docs/                      # IA, ERD, API 연결 문서
│   ├── AdminDashboard_IA.png
│   ├── earthuu_erd.png
│   └── api-mapping.md
└── README.md
```

## 실행

`python3 -m http.server 8765 --directory frontend`

프로젝트 폴더에서 실행한 뒤 브라우저로 http://localhost:8765 에 접속합니다. `frontend/index.html`을 직접 열 수도 있습니다.

- 대시보드: 현재 업무 건수, 기간별 예시 통계
- 이벤트 심사: 검색/필터/정렬, 심사 시작, 승인, 필수 사유 포함 불가 판정
- 이벤트 관리: 심사 결과와 운영 상태 분리, 참가자 조회, 폐기 이력
- 신고 관리: 접수 → 검토 중 → 처리 완료, 사유 및 이력, 운영 중단 예시
- 통계: 기간/채널 선택, 활성 사용자 예시
- 밝은 테마와 다크모드. 테마만 이 브라우저에 저장됩니다.
- 처리 내역은 메모리에만 존재하며 새로고침하면 초기화됩니다. 처리 버튼은 중복 실행을 방지합니다.
- 로그인은 데모입니다. admin@earthuu.demo / earthuu-demo

## ERD 반영과 운영 연결 시 필요한 항목

이벤트는 events.moderation_status와 lifecycle_status를 별도로 표현합니다. 원문 버전은 event_versions, 심사 이력은 moderation_actions, 신청자는 participations, 신고 처리와 조치는 reports / event_dispositions 구조를 참고했습니다. 사용자 요청에 따라 info_cards 콘텐츠 관리는 제외했습니다.

실제 연결에는 API 명세, 서버 권한 검증, 인증/세션 만료, 상태 전이의 원자적 처리, 감사 로그, 통계 수집과 사용자 중복 제거가 필요합니다. 시제품의 숫자는 생성한 예시입니다. 현재 데이터는 PostgreSQL ERD를 실제로 구현한 DB가 아닙니다.

화면에 출력되는 예시 필드는 HTML 이스케이프 처리하며, 관련 이벤트가 없는 신고도 상세 화면을 열 수 있습니다. 실제 API 연결 시에는 각 액션의 로딩·실패·재시도 상태를 서버 응답에 맞춰 추가해야 합니다.

미정: 승인 후 부분 수정 정책(현재는 기존 수정 불가 적용), 호스트 취소 시 참가자 처리/알림, 신고 제재 권한과 공개 범위, 활성 사용자 정의.

WebMCP: 지원 브라우저에서 `view_admin_section` 탐색 도구를 제공합니다.

## 백엔드 및 검수 문서

- 백엔드 구성 안내: [`backend/README.md`](backend/README.md)
- 현재 작업 현황 및 다음 작업: [`docs/project-progress.md`](docs/project-progress.md)
- 화면–API–ERD 연결 초안: [`docs/api-mapping.md`](docs/api-mapping.md)
- ERD: 
- 관리자 IA:
