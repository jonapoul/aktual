#!/usr/bin/env bash
#
# Runs testAll on all modules where Kotlin files have changed on the current branch (vs main).
#
# Usage:
#   ./scripts/test.sh              # Run testAll on changed modules
#   ./scripts/test.sh --dry-run    # Just print the gradle command
#   ./scripts/test.sh develop      # Compare against a different base branch
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=lib/modules.sh
. "$SCRIPT_DIR/lib/modules.sh"

DRY_RUN=false
MAIN_BRANCH="main"

for arg in "$@"; do
  case "$arg" in
    --dry-run) DRY_RUN=true ;;
    *) MAIN_BRANCH="$arg" ;;
  esac
done

run_changed_module_task testAll "$MAIN_BRANCH" "$DRY_RUN"
