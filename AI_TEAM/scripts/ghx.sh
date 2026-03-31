#!/usr/bin/env bash
set -euo pipefail

ROOT="/Users/oyj/Desktop/workspace/AI_TEAM"
ENV_FILE="$ROOT/.env"

if [ -f "$ENV_FILE" ]; then
  set -a
  . "$ENV_FILE"
  set +a
fi

: "${GH_TOKEN:?GH_TOKEN is not set}"

exec gh "$@"
