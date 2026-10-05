# Oracle Cloud 운영 배포

단일 Oracle Cloud VM에서 Docker Compose로 프론트엔드, Spring Boot, PostgreSQL, Nginx를 실행합니다. 외부에는 Nginx의 80/443만 공개되며 8080과 5432는 Docker 내부 네트워크에서만 사용합니다.

## 1. 서버와 방화벽

- DNS의 A 레코드를 VM 공인 IP로 연결합니다.
- Oracle Cloud NSG/보안 목록 인바운드는 TCP 22(관리자 IP만), 80, 443만 허용합니다.
- Ubuntu라면 `ufw default deny incoming`, `ufw allow from <관리자-IP> to any port 22 proto tcp`, `ufw allow 80/tcp`, `ufw allow 443/tcp`, `ufw enable`을 적용합니다.
- 8080과 5432 규칙은 만들지 않습니다. Compose에도 해당 포트의 `ports` 매핑이 없습니다.

## 2. 비밀값과 DB TLS

```bash
cd /opt/earthuu-admin/deploy
cp .env.production.example .env.production
chmod 600 .env.production
./scripts/init-db-tls.sh
```

`.env.production`의 도메인, 이메일, DB/관리자 비밀번호를 바꿉니다. 다음 명령으로 각각 생성할 수 있습니다.

```bash
openssl rand -base64 36
```

초기 관리자 생성 후에는 `BOOTSTRAP_ADMIN_ENABLED=false`로 바꾸고 스택을 다시 시작하세요. `.env.production`, `secrets/`, `letsencrypt/`, `backups/`는 Git에서 제외됩니다.

## 3. 최초 인증서와 실행

Nginx의 HTTPS 설정은 인증서가 필요하므로 최초 한 번은 DNS 연결을 확인한 뒤 Certbot standalone 방식으로 발급합니다. 발급 중에는 80 포트를 사용하는 다른 프로세스가 없어야 합니다.

```bash
./scripts/init-certificates.sh
docker compose --env-file .env.production up -d --build
```

인증서 갱신도 standalone 방식을 사용합니다. 갱신 스크립트는 인증서 만료가 30일보다 더 남으면 아무 작업 없이 종료하고, 갱신이 필요할 때만 Nginx를 잠시 중지하여 80 포트를 Certbot에 넘긴 뒤 반드시 다시 시작합니다.

```bash
./scripts/renew-certificates.sh
```

`earthuu-cert-renew.timer`가 매일 두 번 스크립트를 호출하지만 실제 갱신과 Nginx 중지는 만료 30일 이내에만 발생합니다.

## 4. 자동 시작, 백업, 로그

서비스 파일의 `/opt/earthuu-admin`이 실제 배치 경로와 다르면 수정한 후 설치합니다.

```bash
sudo cp systemd/earthuu-admin.service systemd/earthuu-backup.* \
  systemd/earthuu-cert-renew.* /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now earthuu-admin.service earthuu-backup.timer earthuu-cert-renew.timer
```

- 모든 컨테이너는 `restart: unless-stopped` 정책을 사용합니다.
- 컨테이너 로그는 파일당 10MB, 최대 5개로 순환합니다.
- DB 백업은 매일 03:15에 생성되고 14일 후 삭제됩니다. 운영에서는 `/opt/earthuu-admin/deploy/backups`를 OCI Object Storage 같은 별도 저장소에도 복제하세요.
- 인증서 만료 여부는 매일 03:30, 15:30에 확인하며 30일 이내에만 갱신합니다.
- 상태 확인: `docker compose --env-file .env.production ps`, `docker compose --env-file .env.production logs --tail=200 backend`
- 복구 예: `pg_restore --clean --if-exists -U "$POSTGRES_USER" -d "$POSTGRES_DB" /backups/<파일>.dump`

## 운영 보안 동작

- 운영 프로필은 HTTPS 세션 쿠키를 강제합니다.
- Swagger/OpenAPI는 운영에서 생성 자체를 끄고, Nginx도 관련 경로를 404로 차단합니다. 로컬 개발 환경에서는 기존처럼 접근할 수 있습니다.
- PostgreSQL은 SCRAM 비밀번호와 TLS 연결만 허용하고, 백엔드는 CA와 호스트명을 검증합니다.
- `/actuator/health`는 컨테이너 내부에서만 도달 가능하며 Nginx는 외부에 프록시하지 않습니다.
