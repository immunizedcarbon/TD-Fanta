#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

run_step() {
  local label="$1"
  shift
  echo "==> ${label}"
  "$@"
}

run_step "clean" timeout 900 ./gradlew clean --console=plain
run_step "assembleDebug" timeout 1200 ./gradlew assembleDebug --console=plain
run_step "test" timeout 1200 ./gradlew test --console=plain
run_step "lint" timeout 1200 ./gradlew lint --console=plain

if [[ "${RUN_CONNECTED_TESTS:-0}" == "1" ]]; then
  run_step "connectedDebugAndroidTest" timeout 900 ./gradlew connectedDebugAndroidTest --console=plain
fi

echo "==> verification complete"
