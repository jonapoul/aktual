# shellcheck shell=bash
# Helpers for mapping changed files to Gradle modules and running a per-module task.
# Source from a bash script (`. "$SCRIPT_DIR/lib/modules.sh"`); uses associative arrays,
# so this is bash 4+ only, not POSIX sh. Pulls in git.sh for the change-detection helpers.
# Sourcing it moves to the repo root, since git paths and ./gradlew are relative to it.

if ((BASH_VERSINFO[0] < 4)); then
  echo "Error: needs bash 4+, found $BASH_VERSION (on macOS: brew install bash)" >&2
  exit 1
fi

MODULES_LIB_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=git.sh
. "$MODULES_LIB_DIR/git.sh"

cd "$(git -C "$MODULES_LIB_DIR" rev-parse --show-toplevel)"

# Print every Gradle module path (e.g. :aktual-core:ui, :detekt-rules) declared in
# settings.gradle.kts, sorted. Only matches lines that are exactly a quoted module path (the
# include() blocks) so other quoted ":aktual..." strings elsewhere in the file (kover
# excludedProjects, atlas pathTransforms, etc.) aren't picked up as phantom or duplicate modules.
all_gradle_modules() {
  sed -nE 's/^[[:space:]]*"(:[^"]+)",?[[:space:]]*$/\1/p' settings.gradle.kts | sort
}

# List of repo-root-relative patterns (in .gitignore syntax) that, when changed, affect
# every module and so should trigger a run across all of them. Callers can point this at a
# narrower file after sourcing, e.g. when a task only depends on a few config files.
GITIGNORE_TRIGGERS="$MODULES_LIB_DIR/.global-triggers"

# Optional regex for the search terms of changed version catalog entries (see catalog.py, e.g.
# libs.versions.detekt). When set, a catalog change triggers a run across all modules if one of
# them matches.
CATALOG_TRIGGER_PATTERN=""

# Paths that never affect a module task (docs, generated module READMEs), dropped before matching.
IGNORED_PATHS_PATTERN='\.md$'

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

# Print the search terms for the version catalog entries changed since the given ref (see
# catalog.py), or "ALL" if the catalog can't be compared.
# Args: $1 = base ref.
catalog_changed_terms() {
  local terms
  if terms=$(python3 "$MODULES_LIB_DIR/catalog.py" "$1" 2>/dev/null); then
    printf '%s\n' "$terms"
  else
    echo "ALL"
  fi
}

# Return 0 if CATALOG_TRIGGER_PATTERN is set and matches one of the given catalog terms.
# Args: $1 = output of catalog_changed_terms.
matches_catalog_trigger() {
  [[ -n "$CATALOG_TRIGGER_PATTERN" && -n "$1" ]] || return 1
  [[ "$1" == "ALL" ]] && return 0
  grep -qE "$CATALOG_TRIGGER_PATTERN" <<< "$1"
}

# Print the Gradle module paths whose build files use one of the given catalog terms, or "ALL" if
# one is used by build-logic or the root build (or the catalog can't be compared). Matching is by
# prefix, so it errs towards more modules.
# Args: $1 = output of catalog_changed_terms.
catalog_affected_modules() {
  local terms="$1"
  [[ -z "$terms" ]] && return 0
  if [[ "$terms" == "ALL" ]]; then
    echo "ALL"
    return 0
  fi

  # `apply false` only puts a plugin on the classpath, the modules applying it are matched below
  local global_uses
  global_uses=$(git grep -hF -f <(printf '%s\n' "$terms") -- \
    build-logic build.gradle.kts settings.gradle.kts 'gradle/*.gradle.kts' \
    | grep -v 'apply false' || true)
  if [[ -n "$global_uses" ]]; then
    echo "ALL"
    return 0
  fi

  local files
  files=$(git grep -lF -f <(printf '%s\n' "$terms") -- 'aktual-*/*.gradle.kts' || true)
  changed_gradle_modules "$files"
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
  done < <(all_gradle_modules)

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
# Runs with --continue so one failing module doesn't stop the rest, then prints a summary
# of the tasks that failed.
# Args: $1 = Gradle task suffix (e.g. detektCheck), $2 = base branch, $3 = dry-run (true/false),
# $4 = force (true/false) to run on all modules regardless of changes.
run_changed_module_task() {
  local task_suffix="$1"
  local base_branch="$2"
  local dry_run="$3"
  local force="${4:-false}"

  local modules
  if [[ "$force" == true ]]; then
    echo "Forced - running on all modules."
    modules=$(all_gradle_modules)
  else
    local merge_base merge_base_short
    merge_base=$(git_merge_base "$base_branch") || return 1
    merge_base_short=$(git_short_sha "$merge_base")

    # Deleted paths are kept, so a module that only lost a file still runs
    local changed_files
    changed_files=$(git_changed_paths "$merge_base" | grep -vE "$IGNORED_PATHS_PATTERN" || true)
    if [[ -z "$changed_files" ]]; then
      echo "No files changed since $base_branch ($merge_base_short), nothing to do."
      return 0
    fi

    # A catalog change is narrowed down to the modules using the changed entries, rather than
    # counting as a global trigger by itself
    local catalog="gradle/libs.versions.toml" catalog_changed=false catalog_terms="" other_files
    if grep -qxF "$catalog" <<< "$changed_files"; then
      catalog_terms=$(catalog_changed_terms "$merge_base")
      if matches_gitignore_triggers <<< "$catalog"; then
        catalog_changed=true
      fi
    fi
    other_files=$(grep -vxF "$catalog" <<< "$changed_files" || true)

    local catalog_modules=""
    if [[ "$catalog_changed" == true ]]; then
      catalog_modules=$(catalog_affected_modules "$catalog_terms")
    fi
    if [[ -n "$other_files" ]] && matches_gitignore_triggers <<< "$other_files" \
      || matches_catalog_trigger "$catalog_terms" \
      || [[ "$catalog_modules" == "ALL" ]]; then
      echo "Build/config files changed since $base_branch ($merge_base_short) - running on all modules."
      modules=$(all_gradle_modules)
    else
      if [[ "$catalog_changed" == true ]]; then
        echo "Version catalog changed - $(grep -c . <<< "$catalog_modules" || true) module(s) use the changed entries."
      fi
      modules=$(printf '%s\n' "$(changed_gradle_modules "$other_files")" "$catalog_modules" | grep . | sort -u || true)
    fi
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
    echo "./gradlew --continue ${tasks[*]}"
    return 0
  fi

  # Stream the build but keep a copy, so the failures can be picked back out of it afterwards.
  # `set -e` is off around the pipeline so a failing build doesn't abort before the summary;
  # PIPESTATUS[0] is gradlew's own status rather than tee's.
  local log status
  log=$(mktemp)
  set +e
  ./gradlew --continue "${tasks[@]}" 2>&1 | tee "$log"
  status=${PIPESTATUS[0]}
  set -e

  if [[ "$status" -ne 0 ]]; then
    # Gradle reports each failure as "Execution failed for task ':foo:bar'", spread over
    # separate blocks at the end of the build. Collect the task paths into one short list.
    local failed
    failed=$(sed -nE "s/.*Execution failed for task '([^']+)'.*/\1/p" "$log" | sort -u)
    if [[ -n "$failed" ]]; then
      echo ""
      echo "Failed task(s):"
      while IFS= read -r task; do
        echo "  $task"
      done <<< "$failed"
    fi
  fi

  rm -f "$log"
  return "$status"
}
