# Earthuu 관리자 시스템

IA와 ERD를 기준으로 구현하는 Earthuu 관리자 시스템입니다. React 관리자 화면과 Java 21/Spring Boot API를 함께 구성합니다.

## 프로젝트 구조

```text
earthuu-admin/
├── frontend/                  # React + Vite + TypeScript
│   ├── index.html
│   ├── style.css
│   └── src/
│       ├── api/
│       ├── components/
│       ├── pages/
│       └── store/
├── backend/                   # Java 21 + Spring Boot API
│   ├── src/main/java/com/earthuu/admin/
│   │   ├── auth/
│   │   ├── event/
│   │   └── global/
│   ├── src/main/resources/db/migration/
│   └── src/test/java/com/earthuu/admin/
└── docs/                      # IA, ERD, API 연결 문서
```

## 로컬 실행

PostgreSQL과 백엔드를 먼저 실행합니다.

```bash
cd backend
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export DB_URL=jdbc:postgresql://localhost:5432/earthuu
export DB_USERNAME=earthuu
export DB_PASSWORD=earthuu
export BOOTSTRAP_ADMIN_ENABLED=true
export BOOTSTRAP_ADMIN_EMAIL=admin@earthuu.local
export BOOTSTRAP_ADMIN_PASSWORD=change-me
./mvnw spring-boot:run
```

다른 터미널에서 React 개발 서버를 실행합니다.

```bash
cd frontend
npm install
npm run dev
```

브라우저에서 <http://localhost:5173>에 접속합니다. React 개발 중에는 정적 HTTP 서버가 아니라 `npm run dev`를 사용해야 API 프록시와 TypeScript 빌드가 적용됩니다.

## 현재 연결 범위

- 로그인: Spring Security 서버 세션 + CSRF
- 이벤트 목록·상세·참가자·심사 시작·승인·반려: PostgreSQL API
- 심사 이력·감사 로그: PostgreSQL 트랜잭션 저장
- 이벤트 동시 처리: `events.revision` 기반 낙관적 잠금
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- 신고·통계: 아직 프론트 예시 데이터

이벤트는 `events.moderation_status`와 `lifecycle_status`를 별도로 표현합니다. 원문 버전은 `event_versions`, 심사 이력은 `moderation_actions`, 신청자는 `participations`, 관리자 작업은 `audit_logs`에 저장합니다.

## 검증

```bash
cd backend && ./mvnw test
cd frontend && npm run build
```

미정 사항은 승인 후 부분 수정 정책, 호스트 취소 시 참가자 처리·알림, 신고 제재 권한과 공개 범위, 활성 사용자 정의입니다.

## 문서

- [백엔드 실행 및 API 안내](backend/README.md)
- [화면–API–ERD 연결](docs/api-mapping.md)
