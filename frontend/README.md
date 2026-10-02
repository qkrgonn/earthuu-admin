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
- `src/store`: 데모 데이터와 화면 상태
- `src/api`: Spring API 호출 계층
- `src/types`: API 및 화면 데이터 타입
- `src/mock`: 백엔드 연결 전 예시 데이터

현재는 예시 데이터를 메모리에서 사용합니다. 백엔드 구현 후 `src/store/AdminContext.tsx`의 상태 변경 로직을 `src/api` 호출로 교체합니다. 개발 서버는 `/api` 요청을 `http://localhost:8080`으로 전달합니다.
