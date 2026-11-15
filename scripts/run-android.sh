#!/usr/bin/env bash
#
# Builds the debug app, installs it and launches it on every connected device.
#
# Usage:
#   ./scripts/run-android.sh
#

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
ACTIVITY="dev.jonpoulton.aktual.app.dev/aktual.app.android.AktualActivity"

mapfile -t DEVICES < <(adb devices | awk 'NR > 1 && $2 == "device" { print $1 }')
if [ ${#DEVICES[@]} -eq 0 ]; then
  echo "No connected devices" >&2
  exit 1
fi

# installDebug installs on every connected device
"$ROOT_DIR/gradlew" -p "$ROOT_DIR" :aktual-app:android:installDebug

for device in "${DEVICES[@]}"; do
  echo "Launching on $device"
  adb -s "$device" shell am start -n "$ACTIVITY"
done
