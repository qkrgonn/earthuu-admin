#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
DEPLOY_DIR=$(dirname "$SCRIPT_DIR")
TLS_DIR="$DEPLOY_DIR/secrets/db"

mkdir -p "$TLS_DIR"
umask 077

openssl genrsa -out "$TLS_DIR/ca.key" 4096
openssl req -x509 -new -nodes -key "$TLS_DIR/ca.key" -sha256 -days 3650 \
  -subj "/CN=Earthuu DB CA" -out "$TLS_DIR/ca.crt"
openssl genrsa -out "$TLS_DIR/server.key" 2048
openssl req -new -key "$TLS_DIR/server.key" -subj "/CN=db" -out "$TLS_DIR/server.csr"
printf 'subjectAltName=DNS:db\nextendedKeyUsage=serverAuth\n' > "$TLS_DIR/server.ext"
openssl x509 -req -in "$TLS_DIR/server.csr" -CA "$TLS_DIR/ca.crt" \
  -CAkey "$TLS_DIR/ca.key" -CAcreateserial -out "$TLS_DIR/server.crt" \
  -days 825 -sha256 -extfile "$TLS_DIR/server.ext"
rm "$TLS_DIR/server.csr" "$TLS_DIR/server.ext" "$TLS_DIR/ca.srl"
chmod 600 "$TLS_DIR/server.key" "$TLS_DIR/ca.key"
chmod 644 "$TLS_DIR/server.crt" "$TLS_DIR/ca.crt"

# postgres:alpine의 postgres 사용자는 UID 70입니다. root 소유 키도 허용되지만,
# rootless Docker 등에서는 읽기 권한 문제가 생길 수 있어 서버 키만 해당 UID로 맞춥니다.
if [ "$(id -u)" -eq 0 ]; then
  chown 70:70 "$TLS_DIR/server.key" "$TLS_DIR/server.crt"
fi

echo "DB TLS certificates created in $TLS_DIR"
