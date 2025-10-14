#!/bin/sh

# Run, merge all submodule reports and filter to show those with upgrades available
SCRIPT_DIR="$(dirname "$0")"
cd "$SCRIPT_DIR/.." || exit

echo "Running..."
./gradlew dependencyUpdates --no-parallel --quiet > /dev/null
find . -name "report.txt" -type f -exec cat {} + | grep '\->' | sort -u
