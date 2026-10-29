#!/usr/bin/env bash
# Runs a Gradle command and returns only actionable output (errors, failures, warnings).
# The full output is kept in a log file, so nothing needs a second run to recover.
# Usage: ./scripts/gradle-run.sh :<module>:<task> [extra flags...]
set -o pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT" || exit 1

MAX_LINES=200
# Kotlin compiler, Gradle, test, lint ("Error:", "Warning:") and detekt ("file:line:col [Rule] ...")
# output
PATTERN="^e: |^w: |[Ee]rror:|[Ww]arning:|Unresolved reference|None of the following candidates|Could not resolve|Caused by:|Exception|AssertionError|expected:|but was:|FAILED|passed|BUILD SUCCESS|BUILD FAILED|> Task :.*FAILED|at aktual\.|:[0-9]+:[0-9]+ \[[A-Za-z]+\] "

LOG_DIR="$ROOT/build/gradle-run"
mkdir -p "$LOG_DIR"
# PID suffix keeps concurrent runs started in the same second apart
LOG="$LOG_DIR/$(date +%Y%m%d-%H%M%S)-$$.log"

./gradlew "$@" > "$LOG" 2>&1
EXIT=$?

# Lines matching PATTERN, plus every "* What went wrong:" block up to its "* Try:" line, since
# configuration failures (unknown task, missing plugin) match nothing else
filtered=$(PATTERN="$PATTERN" awk '
  /^\* What went wrong:/ { block = 1 }
  /^\* Try:/ { block = 0 }
  block || $0 ~ ENVIRON["PATTERN"]
' "$LOG")

matches=$(printf '%s\n' "$filtered" | grep -c .)
printf '%s\n' "$filtered" | grep . | head -n "$MAX_LINES"
if [ "$matches" -gt "$MAX_LINES" ]; then
  echo "TRUNCATED: showing $MAX_LINES of $matches matching lines"
fi
echo "BUILD $([ $EXIT -eq 0 ] && echo SUCCESS || echo FAILED) (exit $EXIT)"
echo "Full log ($(wc -l < "$LOG") lines): $LOG"
exit $EXIT
