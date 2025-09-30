#!/bin/sh

# Run, merge all submodule reports and filter to show those with upgrades available
cd ..
echo "Running..."
./gradlew dependencyUpdates --no-parallel --quiet > /dev/null
find . -name "report.txt" -type f -exec cat {} + | grep '\->' | sort -u
