#!/bin/sh

SCRIPT_DIR="$(dirname "$0")"
cd "$SCRIPT_DIR/.." || exit

gradle-profiler \
  --profile async-profiler \
  --project-dir . \
  --scenario-file profiling/configuration.scenario \
  --gradle-user-home profiling/gradle-user-home \
  --output-dir profiling/profile-out
