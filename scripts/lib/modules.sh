# shellcheck shell=bash
# Helpers for mapping changed files to Gradle modules and running a per-module task.
# Source from a bash script (`. "$SCRIPT_DIR/lib/modules.sh"`); uses associative arrays,
# so this is bash-only, not POSIX sh. Pulls in git.sh for the change-detection helpers.

MODULES_LIB_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=git.sh
. "$MODULES_LIB_DIR/git.sh"

# Print every Gradle module path (e.g. :aktual-core:ui) declared in settings.gradle.kts, sorted.
all_gradle_modules() {
  grep -oP '":aktual[^"]*' settings.gradle.kts | tr -d '"' | sort
}

# List of repo-root-relative patterns (in .gitignore syntax) that, when changed, affect
# every module and so should trigger a run across all of them.
GITIGNORE_TRIGGERS="$MODULES_LIB_DIR/.global-triggers"

# Return 0 if any of the newline-separated paths on stdin match a pattern in
# .global-triggers. Matching is delegated to `git check-ignore` against a throwaway
# repo whose only ignore source is that file, so the repo's own .gitignore can't interfere.
matches_gitignore_triggers() {
  [[ -f "$GITIGNORE_TRIGGERS" ]] || return 1
  local tmp rc=1
  tmp="$(mktemp -d)"
  git init -q "$tmp"
  cp "$GITIGNORE_TRIGGERS" "$tmp/.gitignore"
  if git -C "$tmp" -c core.excludesFile=/dev/null check-ignore --no-index --quiet --stdin; then
    rc=0
  fi
  rm -rf "$tmp"
  return "$rc"
}

# Print the Gradle module paths (e.g. :aktual-core:ui) that own the given changed files.
# Reads module paths from settings.gradle.kts in the current working directory.
# Args: $1 = newline-separated list of changed file paths (relative to repo root).
# Output: sorted, de-duplicated Gradle module paths, one per line.
changed_gradle_modules() {
  local changed_files="$1"

  # Read all module Gradle paths from settings.gradle.kts and map to disk paths.
  # Gradle paths use colons (e.g. :aktual-core:ui), disk paths use slashes (e.g. aktual-core/ui).
  local -A module_map
  local gradle_path disk_path
  while IFS= read -r gradle_path; do
    disk_path="${gradle_path#:}"
    disk_path="${disk_path//:/\/}"
    module_map["$disk_path"]="$gradle_path"
  done < <(grep -oP '":aktual[^"]*' settings.gradle.kts | tr -d '"')

  # Sort module disk paths longest-first so deeper modules match before their parents
  local sorted_disk_paths
  sorted_disk_paths=$(printf '%s\n' "${!module_map[@]}" | awk '{ print length, $0 }' | sort -rn | cut -d' ' -f2-)

  # Match changed files to modules
  local -A seen
  local file matched=()
  while IFS= read -r file; do
    [[ -z "$file" ]] && continue
    while IFS= read -r disk_path; do
      if [[ "$file" == "$disk_path/"* ]]; then
        gradle_path="${module_map[$disk_path]}"
        if [[ -z "${seen[$gradle_path]:-}" ]]; then
          seen["$gradle_path"]=1
          matched+=("$gradle_path")
        fi
        break
      fi
    done <<< "$sorted_disk_paths"
  done <<< "$changed_files"

  if [[ ${#matched[@]} -gt 0 ]]; then
    printf '%s\n' "${matched[@]}" | sort
  fi
}

# Run a per-module Gradle task on every module with changed files (vs a base branch).
# Args: $1 = Gradle task suffix (e.g. detektCheck), $2 = base branch, $3 = dry-run (true/false).
run_changed_module_task() {
  local task_suffix="$1"
  local base_branch="$2"
  local dry_run="$3"

  local merge_base merge_base_short
  merge_base=$(git_merge_base "$base_branch")
  merge_base_short=$(git_short_sha "$merge_base")

  local changed_files
  changed_files=$(git_changed_files "$merge_base")
  if [[ -z "$changed_files" ]]; then
    echo "No files changed since $base_branch ($merge_base_short), nothing to do."
    return 0
  fi

  local modules
  if printf '%s\n' "$changed_files" | matches_gitignore_triggers; then
    echo "Build/config files changed since $base_branch ($merge_base_short) — running on all modules."
    modules=$(all_gradle_modules)
  else
    modules=$(changed_gradle_modules "$changed_files")
  fi
  if [[ -z "$modules" ]]; then
    echo "Changed files don't belong to any known module."
    return 0
  fi

  local tasks=() gradle_path
  while IFS= read -r gradle_path; do
    tasks+=("${gradle_path}:${task_suffix}")
  done <<< "$modules"

  echo "Running $task_suffix on ${#tasks[@]} module(s):"
  local task
  for task in "${tasks[@]}"; do
    echo "  $task"
  done
  echo ""

  if [[ "$dry_run" == true ]]; then
    echo "./gradlew ${tasks[*]}"
  else
    exec ./gradlew "${tasks[@]}"
  fi
}
