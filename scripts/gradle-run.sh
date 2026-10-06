#!/usr/bin/env bash
# Runs a Gradle command and returns only actionable output (errors, failures, warnings).
# The full output is kept in a log file, so nothing needs a second run to recover.
# Usage: ./scripts/gradle-run.sh :<module>:<task> [extra flags...]
set -o pipefail

MAX_LINES=200
PATTERN="^e: |^w: |error:|warning:|Unresolved reference|None of the following candidates|Could not resolve|Caused by:|Exception|AssertionError|expected:|but was:|FAILED|passed|BUILD SUCCESS|BUILD FAILED|> Task :.*FAILED|at aktual\."

LOG_DIR="$(cd "$(dirname "$0")/.." && pwd)/build/gradle-run"
mkdir -p "$LOG_DIR"
LOG="$LOG_DIR/$(date +%Y%m%d-%H%M%S).log"

./gradlew "$@" > "$LOG" 2>&1
EXIT=$?

matches=$(grep -cE "$PATTERN" "$LOG")
grep -E "$PATTERN" "$LOG" | head -"$MAX_LINES"
if [ "$matches" -gt "$MAX_LINES" ]; then
  echo "TRUNCATED: showing $MAX_LINES of $matches matching lines"
fi
echo "BUILD $([ $EXIT -eq 0 ] && echo SUCCESS || echo FAILED) (exit $EXIT)"
echo "Full log ($(wc -l < "$LOG") lines): $LOG"
exit $EXIT
