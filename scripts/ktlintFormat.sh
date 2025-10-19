#!/bin/sh

SCRIPT_DIR="$(dirname "$0")"
cd "$SCRIPT_DIR/.." || exit

command -v ktlint >/dev/null 2>&1 || { echo "ktlint not found"; exit 1; }

ktlint '**/*.kt' '**/*.kts' '!**/build/**' --color --format
