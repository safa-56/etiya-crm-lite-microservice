#!/bin/sh
# =============================================================================
# Debezium connector'larini otomatik kaydeder (idempotent).
#
# infra/debezium/register-*.json dosyalarinin her birini Kafka Connect REST
# API'sine gonderir. Compose'da `debezium-registrar` servisi tarafindan bir kez
# calistirilir; elle de calistirilabilir.
#
# Idempotent: connector zaten varsa config PUT ile guncellenir, yeniden
# olusturulmaz (replication slot ve offset'ler korunur).
#
# Bekleme: connector'lar `outbox_events` tablosunu ister; tablo henuz yoksa
# (servis ilk kez ayaga kalkiyorsa) task FAILED olur. Script bu durumda
# connector'i yeniden baslatarak RUNNING olana kadar dener.
# =============================================================================
set -eu

CONNECT_URL="${CONNECT_URL:-http://debezium-connect:8083}"
CONNECTOR_DIR="${CONNECTOR_DIR:-/connectors}"
# Connect REST API'sinin ayaga kalkmasi icin beklenecek azami sure (saniye).
CONNECT_WAIT_SECONDS="${CONNECT_WAIT_SECONDS:-180}"
# Bir connector'in RUNNING olmasi icin yapilacak azami deneme sayisi.
MAX_ATTEMPTS="${MAX_ATTEMPTS:-30}"
# Denemeler arasi bekleme (saniye).
RETRY_INTERVAL="${RETRY_INTERVAL:-10}"

log() { echo "[registrar] $*"; }

# --- 1) Connect REST API hazir mi? ------------------------------------------
log "Kafka Connect bekleniyor: $CONNECT_URL"
waited=0
until curl -sf -o /dev/null "$CONNECT_URL/connectors"; do
  if [ "$waited" -ge "$CONNECT_WAIT_SECONDS" ]; then
    log "HATA: Connect $CONNECT_WAIT_SECONDS sn icinde hazir olmadi."
    exit 1
  fi
  sleep 3
  waited=$((waited + 3))
done
log "Kafka Connect hazir."

# JSON dosyasindan connector adini okur ("name": "x-outbox-connector").
connector_name() {
  sed -n 's/.*"name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' "$1" | head -n 1
}

# JSON dosyasindan yalnizca "config" nesnesini ayiklar (PUT /config icin).
# jq bagimliligi olmasin diye: ilk satirlari ("name" ve "config": {) atar,
# son satiri (kapanis }) atar.
connector_config() {
  sed -n '/"config"[[:space:]]*:[[:space:]]*{/,$p' "$1" \
    | sed '1s/.*"config"[[:space:]]*:[[:space:]]*{/{/' \
    | sed '$d'
}

# --- 2) Her connector'i kaydet ----------------------------------------------
failed=0
for file in "$CONNECTOR_DIR"/register-*.json; do
  [ -f "$file" ] || continue
  name="$(connector_name "$file")"
  if [ -z "$name" ]; then
    log "ATLANDI: $file icinde \"name\" alani yok."
    failed=1
    continue
  fi

  log "--- $name ---"
  config="$(connector_config "$file")"

  attempt=1
  while [ "$attempt" -le "$MAX_ATTEMPTS" ]; do
    # PUT /connectors/<name>/config: yoksa olusturur, varsa gunceller.
    code="$(printf '%s' "$config" | curl -s -o /tmp/resp.json -w '%{http_code}' \
      -X PUT "$CONNECT_URL/connectors/$name/config" \
      -H 'Content-Type: application/json' --data-binary @-)"

    if [ "$code" != "200" ] && [ "$code" != "201" ]; then
      log "kayit denemesi $attempt/$MAX_ATTEMPTS basarisiz (HTTP $code): $(cat /tmp/resp.json)"
      sleep "$RETRY_INTERVAL"
      attempt=$((attempt + 1))
      continue
    fi

    # Task'lar gercekten calisiyor mu? (outbox_events tablosu yoksa FAILED olur)
    sleep 3
    status="$(curl -s "$CONNECT_URL/connectors/$name/status")"
    case "$status" in
      *'"state":"FAILED"'*)
        log "task FAILED (deneme $attempt/$MAX_ATTEMPTS) — outbox_events tablosu"
        log "  henuz olusmamis olabilir; $RETRY_INTERVAL sn sonra restart edilecek."
        curl -s -o /dev/null -X POST "$CONNECT_URL/connectors/$name/restart?includeTasks=true&onlyFailed=true" || true
        sleep "$RETRY_INTERVAL"
        attempt=$((attempt + 1))
        ;;
      *'"tasks":[]'*)
        log "task henuz atanmadi (deneme $attempt/$MAX_ATTEMPTS), bekleniyor..."
        sleep "$RETRY_INTERVAL"
        attempt=$((attempt + 1))
        ;;
      *)
        log "OK: $name calisiyor."
        break
        ;;
    esac
  done

  if [ "$attempt" -gt "$MAX_ATTEMPTS" ]; then
    log "HATA: $name RUNNING durumuna gelmedi."
    failed=1
  fi
done

if [ "$failed" -ne 0 ]; then
  log "Bazi connector'lar kaydedilemedi. Durum: $CONNECT_URL/connectors"
  exit 1
fi

log "Tum connector'lar kayitli ve calisiyor."
