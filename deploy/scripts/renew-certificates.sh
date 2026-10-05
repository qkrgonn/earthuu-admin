#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
DEPLOY_DIR=$(dirname "$SCRIPT_DIR")
cd "$DEPLOY_DIR"

set -a
. ./.env.production
set +a

: "${DOMAIN:?DOMAIN is required in .env.production}"

CERTIFICATE="./letsencrypt/live/$DOMAIN/fullchain.pem"
if [ ! -f "$CERTIFICATE" ]; then
  echo "Certificate not found: $CERTIFICATE" >&2
  echo "Run ./scripts/init-certificates.sh first." >&2
  exit 1
fi

# 인증서 만료가 30일보다 더 남았다면 Nginx를 중지하지 않습니다.
if openssl x509 -checkend 2592000 -noout -in "$CERTIFICATE"; then
  echo "Certificate for $DOMAIN is valid for more than 30 days; renewal skipped."
  exit 0
fi

compose() {
  docker compose --env-file .env.production "$@"
}

NGINX_WAS_RUNNING=false
if compose ps --status running --services | grep -qx nginx; then
  NGINX_WAS_RUNNING=true
  compose stop nginx
fi

restore_nginx() {
  if [ "$NGINX_WAS_RUNNING" = true ]; then
    compose start nginx >/dev/null 2>&1 || true
  fi
}
trap restore_nginx EXIT HUP INT TERM

compose --profile tools run --rm --service-ports certbot renew --quiet

restore_nginx
trap - EXIT HUP INT TERM

echo "TLS certificate renewal completed for $DOMAIN"
