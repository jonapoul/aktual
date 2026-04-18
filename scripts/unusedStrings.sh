#!/usr/bin/env bash
#
# Scans the auto-generated Strings.kt and Plurals.kt catalogs and reports any
# entries that aren't referenced anywhere in the project sources. For CI.
#
# Usage:
#   ./scripts/unusedStrings.sh          # Report unused, exit non-zero if any
#   ./scripts/unusedStrings.sh --list   # Just print unused names, no header
#

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
GENERATED_DIR="$REPO_ROOT/aktual-core/l10n/build/generated/kotlin/catalogCommonMain/aktual/core/l10n"
STRINGS_FILE="$GENERATED_DIR/Strings.kt"
PLURALS_FILE="$GENERATED_DIR/Plurals.kt"

LIST_ONLY=false
for arg in "$@"; do
  case "$arg" in
    --list) LIST_ONLY=true ;;
    *) echo "Unknown arg: $arg" >&2; exit 2 ;;
  esac
done

for f in "$STRINGS_FILE" "$PLURALS_FILE"; do
  if [[ ! -f "$f" ]]; then
    echo "Generated file not found: $f" >&2
    echo "Run: ./gradlew :aktual-core:l10n:catalog" >&2
    exit 1
  fi
done

# Collect every .kt file under any module's src/ directory, excluding build/.
mapfile -t SOURCES < <(find "$REPO_ROOT" \
  -type d \( -name build -o -name .git -o -name .gradle -o -name symlinks \) -prune -o \
  -type f -name '*.kt' -path '*/src/*' -print)

if [[ ${#SOURCES[@]} -eq 0 ]]; then
  echo "No Kotlin sources found" >&2
  exit 1
fi

unused_all=()
total_count=0

# check_catalog <container> <file> <extract_sed_script>
# Extracts entry names via the sed script, then looks for `<container>.<name>`
# or `import aktual.core.l10n.<container>.<name>` across all sources.
check_catalog() {
  local container="$1"
  local file="$2"
  local extract_sed="$3"

  mapfile -t entries < <(sed -n "$extract_sed" "$file")

  if [[ ${#entries[@]} -eq 0 ]]; then
    echo "No entries found in $file" >&2
    exit 1
  fi

  total_count=$((total_count + ${#entries[@]}))

  local alt
  alt=$(IFS='|'; echo "${entries[*]}")
  local usage_pattern="\\b${container}\\.(${alt})\\b"
  local import_pattern="^[[:space:]]*import[[:space:]]+aktual\\.core\\.l10n\\.${container}\\.([A-Za-z_][A-Za-z0-9_]*)"

  declare -A used
  while IFS= read -r name; do
    used["$name"]=1
  done < <(
    {
      grep -hEo "$usage_pattern" "${SOURCES[@]}" | sed -E "s/^${container}\\.//"
      grep -hEo "$import_pattern" "${SOURCES[@]}" | sed -E "s/^[[:space:]]*import[[:space:]]+aktual\\.core\\.l10n\\.${container}\\.//"
    } | sort -u
  )

  for name in "${entries[@]}"; do
    if [[ -z "${used[$name]:-}" ]]; then
      unused_all+=("$name")
    fi
  done
}

# Strings: `  public val <name>:`
check_catalog "Strings" "$STRINGS_FILE" \
  's/^[[:space:]]*public val \([A-Za-z_][A-Za-z0-9_]*\):.*/\1/p'

# Plurals: `  public fun <name>(`
check_catalog "Plurals" "$PLURALS_FILE" \
  's/^[[:space:]]*public fun \([A-Za-z_][A-Za-z0-9_]*\)(.*/\1/p'

if [[ ${#unused_all[@]} -gt 0 ]]; then
  mapfile -t unused_all < <(printf '%s\n' "${unused_all[@]}" | LC_ALL=C sort)
fi

if $LIST_ONLY; then
  printf '%s\n' "${unused_all[@]}"
  [[ ${#unused_all[@]} -eq 0 ]] || exit 1
  exit 0
fi

if [[ ${#unused_all[@]} -eq 0 ]]; then
  echo "All ${total_count} catalog entries are used."
  exit 0
fi

echo "Found ${#unused_all[@]} unused entries out of ${total_count}:"
printf '  %s\n' "${unused_all[@]}"
exit 1
