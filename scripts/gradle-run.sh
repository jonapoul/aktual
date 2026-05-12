#!/usr/bin/env bash
# Runs a Gradle command and returns only actionable output (errors, failures, warnings).
# Usage: ./scripts/gradle-run.sh :<module>:<task> [extra flags...]
set -o pipefail

output=$(./gradlew "$@" 2>&1)
EXIT=$?
echo "$output" | grep -E "^e: |^w: |error:|warning:|Unresolved reference|None of the following candidates|Could not resolve|Caused by:|Exception|AssertionError|expected:|but was:|FAILED|passed|BUILD SUCCESS|BUILD FAILED|> Task :.*FAILED|at aktual\." | head -200
echo "BUILD $([ $EXIT -eq 0 ] && echo SUCCESS || echo FAILED) (exit $EXIT)"
exit $EXIT
