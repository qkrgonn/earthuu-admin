#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
DEPLOY_DIR=$(dirname "$SCRIPT_DIR")
cd "$DEPLOY_DIR"

set -a
. ./.env.production
set +a

mkdir -p backups
STAMP=$(date -u +%Y%m%dT%H%M%SZ)
docker compose --env-file .env.production exec -T db \
  pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc > "backups/earthuu-$STAMP.dump"
find backups -type f -name 'earthuu-*.dump' -mtime +14 -delete
