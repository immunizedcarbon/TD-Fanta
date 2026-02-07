#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

LOG_FILE="${1:-device_connected_test.log}"

cleanup() {
  if [[ -n "${LOGCAT_PID:-}" ]]; then
    kill "${LOGCAT_PID}" >/dev/null 2>&1 || true
    wait "${LOGCAT_PID}" 2>/dev/null || true
  fi
}
trap cleanup EXIT

adb logcat -c
adb logcat -v threadtime > "${LOG_FILE}" 2>&1 &
LOGCAT_PID=$!

timeout 900 ./gradlew connectedDebugAndroidTest --console=plain

echo "logcat written to ${LOG_FILE}"
