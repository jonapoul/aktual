#!/bin/sh

SCRIPT_DIR="$(dirname "$0")"
cd "$SCRIPT_DIR/.." || exit

ktlint '**/*.kt' '**/*.kts' '!**/build/**' --color --format
