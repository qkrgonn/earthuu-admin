# Frontend

HTML, CSS, JavaScript로 구현한 Earthuu 관리자 화면입니다.

## 실행

프로젝트 루트에서 다음 명령을 실행합니다.

```bash
python3 -m http.server 8765 --directory frontend
```

브라우저에서 <http://localhost:8765>에 접속합니다.

현재는 예시 데이터를 메모리에서 사용합니다. 백엔드 구현 후 `app.js`의 데이터와 상태 변경 로직을 `/api/admin/**` 호출로 교체합니다.

