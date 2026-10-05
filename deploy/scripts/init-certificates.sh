#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
DEPLOY_DIR=$(dirname "$SCRIPT_DIR")
cd "$DEPLOY_DIR"

set -a
. ./.env.production
set +a

: "${DOMAIN:?DOMAIN is required in .env.production}"
: "${LETSENCRYPT_EMAIL:?LETSENCRYPT_EMAIL is required in .env.production}"

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

compose --profile tools run --rm --service-ports certbot \
  certonly --standalone --agree-tos --no-eff-email \
  --email "$LETSENCRYPT_EMAIL" -d "$DOMAIN"

restore_nginx
trap - EXIT HUP INT TERM

echo "TLS certificate is ready for $DOMAIN"
