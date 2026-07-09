#!/usr/bin/env bash
# 3 servisin veritabanını test seed'iyle doldurur (crm-postgres container'ı içinden).
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONTAINER="${PG_CONTAINER:-crm-postgres}"
USER="${PG_USER:-postgres}"

load() {
  echo ">> $2 <- $(basename "$1")"
  docker exec -i "$CONTAINER" psql -v ON_ERROR_STOP=1 -U "$USER" -d "$2" < "$1"
}

load "$DIR/01_customer_seed.sql" customerdb
load "$DIR/02_account_seed.sql"  accountdb
load "$DIR/03_product_seed.sql"  productdb

echo "Seed tamamlandı."
