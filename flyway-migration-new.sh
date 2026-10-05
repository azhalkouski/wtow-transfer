#!/usr/bin/env bash
set -euo pipefail

if [ -z "${1:-}" ]; then
  echo "usage: ./flyway-migration-new <name>" >&2
  exit 1
fi

if [ -n "${2:-}" ]; then
  echo "error: multiple arguments detected. Wrap name in quotes or use underscores."
  echo "usage: ./flyway-migration-new \"create users table\""
  echo "   or: ./flyway-migration-new create_users_table"
  exit 1
fi

name="${1// /_}"
timestamp="$(date +%Y%m%d%H%M%S)"
migration_dir="src/main/resources/db/migration"
file="$migration_dir/V${timestamp}__${name}.sql"

mkdir -p "$migration_dir"
touch "$file"
echo "created $file"
