#!/usr/bin/env bash
set -euo pipefail

if [[ -f .env ]]; then
  source .env
fi

: "${WTOW_ADMIN_PW:?WTOW_ADMIN_PW not set - create provisioning/database/.env from .env.example}"
: "${WTOW_SERVICE_PW:?WTOW_SERVICE_PW not set - create provisioning/database/.env from .env.example}"
: "${WTOW_READ_PW:?WTOW_READ_PW not set - create provisioning/database/.env from .env.example}"

psql -v ON_ERROR_STOP=1 \
    -v admin_pw="$WTOW_ADMIN_PW" \
    -v service_pw="$WTOW_SERVICE_PW" \
    -v wtow_read_pw="$WTOW_READ_PW"
    -U postgres -d postgres -f "001-create-db-and-roles.sql"
echo "001-create-db-and-roles.sql executed"

psql -v ON_ERROR_STOP=1 \
    -U postgres -d wallet_to_wallet_transfer -f "002-create-namespace.sql"
echo "002-create-namespace.sql executed"

wtow_roles=$(
psql -v ON_ERROR_STOP=1 -U postgres -d postgres \
    -c "SELECT rolname, pg_catalog.shobj_description(oid, 'pg_authid') AS comment FROM pg_roles WHERE rolname LIKE 'wtow%';"
)
echo "$wtow_roles"
