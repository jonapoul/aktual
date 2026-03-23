#!/usr/bin/env bash
#
# Runs detektCheck on all modules where Kotlin files have changed on the current branch (vs main).
#
# Usage:
#   ./scripts/detekt.sh              # Run detektCheck on changed modules
#   ./scripts/detekt.sh --dry-run    # Just print the gradle command
#   ./scripts/detekt.sh develop      # Compare against a different base branch
#

set -euo pipefail

DRY_RUN=false
MAIN_BRANCH="main"

for arg in "$@"; do
  case "$arg" in
    --dry-run) DRY_RUN=true ;;
    *) MAIN_BRANCH="$arg" ;;
  esac
done

MERGE_BASE=$(git merge-base "$MAIN_BRANCH" HEAD 2>/dev/null || git rev-parse HEAD)
MERGE_BASE_SHORT=$(git rev-parse --short "$MERGE_BASE" 2>/dev/null || echo "unknown")

# If libs.versions.toml changed, detekt config may have changed — run on all modules
RUN_ALL=false
if git diff --name-only "$MERGE_BASE" -- 'gradle/libs.versions.toml' 2>/dev/null | grep -q .; then
  RUN_ALL=true
fi

# Read all module Gradle paths from settings.gradle.kts and map to disk paths.
# Gradle paths use colons (e.g. :aktual-core:ui), disk paths use slashes (e.g. aktual-core/ui).
declare -A module_map
while IFS= read -r gradle_path; do
  disk_path="${gradle_path#:}"
  disk_path="${disk_path//:/\/}"
  module_map["$disk_path"]="$gradle_path"
done < <(grep -oP '":aktual[^"]*' settings.gradle.kts | tr -d '"')

tasks=()

if [[ "$RUN_ALL" == true ]]; then
  echo "libs.versions.toml changed — running detektCheck on all modules"
  for gradle_path in "${module_map[@]}"; do
    tasks+=("${gradle_path}:detektCheck")
  done
else
  # Get all changed .kt/.kts files (committed, uncommitted, and untracked) relative to the base branch
  changed_files=$(
    {
      git diff --name-only "$MERGE_BASE" -- '*.kt' '*.kts' 2>/dev/null
      git ls-files --others --exclude-standard -- '*.kt' '*.kts' 2>/dev/null
    } | sort -u | while IFS= read -r file; do
      [[ -f "$file" ]] && printf '%s\n' "$file"
    done
  )

  if [[ -z "$changed_files" ]]; then
    echo "No Kotlin files changed since $MAIN_BRANCH ($MERGE_BASE_SHORT), nothing to do."
    exit 0
  fi

  # Sort module disk paths longest-first so deeper modules match before their parents
  sorted_disk_paths=$(printf '%s\n' "${!module_map[@]}" | awk '{ print length, $0 }' | sort -rn | cut -d' ' -f2-)

  # Match changed files to modules
  declare -A seen
  while IFS= read -r file; do
    while IFS= read -r disk_path; do
      if [[ "$file" == "$disk_path/"* ]]; then
        gradle_path="${module_map[$disk_path]}"
        if [[ -z "${seen[$gradle_path]:-}" ]]; then
          seen["$gradle_path"]=1
          tasks+=("${gradle_path}:detektCheck")
        fi
        break
      fi
    done <<< "$sorted_disk_paths"
  done <<< "$changed_files"
fi

if [[ ${#tasks[@]} -eq 0 ]]; then
  echo "Changed Kotlin files don't belong to any known module."
  exit 0
fi

# Sort for deterministic output
IFS=$'\n' tasks=($(printf '%s\n' "${tasks[@]}" | sort)); unset IFS

echo "Running detektCheck on ${#tasks[@]} module(s):"
for task in "${tasks[@]}"; do
  echo "  $task"
done
echo ""

if [[ "$DRY_RUN" == true ]]; then
  echo "./gradlew ${tasks[*]}"
else
  exec ./gradlew "${tasks[@]}"
fi
