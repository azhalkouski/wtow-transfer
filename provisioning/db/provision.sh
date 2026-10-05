#!/usr/bin/env bash
set -euo pipefail

if [[ -f .env ]]; then
  source .env
fi

: "${ADMIN_PW:?ADMIN_PW not set - create provisioning/database/.env from .env.example}"
: "${SERVICE_PW:?SERVICE_PW not set - create provisioning/database/.env from .env.example}"

psql -v ON_ERROR_STOP=1 \
    -v admin_pw="$ADMIN_PW" \
    -v service_pw="$SERVICE_PW" \
    -U postgres -d postgres -f "001-create-db-and-roles.sql"
echo "001-create-db-and-roles.sql executed"

psql -v ON_ERROR_STOP=1 \
    -U postgres -d wallet_to_wallet_transfer -f "002-create-namespace.sql"
echo "002-create-namespace.sql executed"
