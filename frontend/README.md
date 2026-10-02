# Frontend

React, Vite, TypeScript로 구현한 Earthuu 관리자 화면입니다.

## 실행

`frontend` 디렉터리에서 다음 명령을 실행합니다.

```bash
npm install
npm run dev
```

브라우저에서 <http://localhost:5173>에 접속합니다.

프로덕션 빌드는 다음 명령으로 확인합니다.

```bash
npm run build
```

## 구조

- `src/pages`: 라우트별 관리자 화면
- `src/components`: 공통 레이아웃과 UI
- `src/store`: 인증·이벤트 API 상태와 화면 상태
- `src/api`: Spring API 호출 계층
- `src/types`: API 및 화면 데이터 타입
- `src/mock`: 아직 API를 연결하지 않은 신고·통계 예시 데이터

관리자 로그인과 이벤트 목록·상세·참가자·심사 처리는 Spring API를 사용합니다. 요청은 서버 세션 쿠키를 포함하며 변경 요청 전 CSRF 토큰을 자동 발급합니다. 신고와 통계 화면은 아직 예시 데이터를 사용합니다.

개발 서버는 `/api` 요청을 `http://localhost:8080`으로 전달하므로 백엔드를 먼저 실행해야 합니다.
