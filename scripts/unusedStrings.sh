#!/usr/bin/env bash
#
# Scans the auto-generated Strings.kt and Plurals.kt catalogs and reports any
# entries that aren't referenced anywhere in the project sources. For CI.
#
# Reports the XML resource name (snake_case) rather than the Kotlin accessor
# (camelCase) so that output is greppable against the strings-*.xml files.
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

# check_catalog <container> <res_kind> <file> <awk_program>
# - container: "Strings" or "Plurals" (the catalog object name)
# - res_kind: "string" or "plurals" (matches Res.<res_kind>.<name>)
#
# The awk program must print "<accessor> <resource>" lines for every entry in
# <file>. An entry counts as used if any source references either:
#   - the catalog accessor:   <container>.<accessor>
#   - the raw resource:       Res.<res_kind>.<resource>
#   - an import of either:    import aktual.core.l10n.<container>.<accessor>
#                             import aktual.core.l10n.<resource>
# Reports the <resource> (snake_case) of any entry with no hits.
check_catalog() {
  local container="$1"
  local res_kind="$2"
  local file="$3"
  local awk_prog="$4"

  mapfile -t pairs < <(awk "$awk_prog" "$file")

  if [[ ${#pairs[@]} -eq 0 ]]; then
    echo "No entries found in $file" >&2
    exit 1
  fi

  total_count=$((total_count + ${#pairs[@]}))

  local -a accessors=() resources=()
  declare -A resource_of
  local pair accessor resource
  for pair in "${pairs[@]}"; do
    accessor="${pair%% *}"
    resource="${pair#* }"
    accessors+=("$accessor")
    resources+=("$resource")
    resource_of["$accessor"]="$resource"
  done

  local acc_alt res_alt
  acc_alt=$(IFS='|'; echo "${accessors[*]}")
  res_alt=$(IFS='|'; echo "${resources[*]}")

  local accessor_pattern="\\b${container}\\.(${acc_alt})\\b"
  local accessor_import_pattern="^[[:space:]]*import[[:space:]]+aktual\\.core\\.l10n\\.${container}\\.([A-Za-z_][A-Za-z0-9_]*)"
  local resource_pattern="\\bRes\\.${res_kind}\\.(${res_alt})\\b"
  local resource_import_pattern="^[[:space:]]*import[[:space:]]+aktual\\.core\\.l10n\\.(${res_alt})\\b"

  declare -A used_accessors used_resources
  while IFS= read -r name; do
    used_accessors["$name"]=1
  done < <(
    {
      grep -hEo "$accessor_pattern" "${SOURCES[@]}" | sed -E "s/^${container}\\.//"
      grep -hEo "$accessor_import_pattern" "${SOURCES[@]}" | sed -E "s/^[[:space:]]*import[[:space:]]+aktual\\.core\\.l10n\\.${container}\\.//"
    } | sort -u
  )
  while IFS= read -r name; do
    used_resources["$name"]=1
  done < <(
    {
      grep -hEo "$resource_pattern" "${SOURCES[@]}" | sed -E "s/^Res\\.${res_kind}\\.//"
      grep -hEo "$resource_import_pattern" "${SOURCES[@]}" | sed -E 's/^[[:space:]]*import[[:space:]]+aktual\.core\.l10n\.//'
    } | sort -u
  )

  for accessor in "${accessors[@]}"; do
    resource="${resource_of[$accessor]}"
    if [[ -z "${used_accessors[$accessor]:-}" && -z "${used_resources[$resource]:-}" ]]; then
      unused_all+=("$resource")
    fi
  done
}

# Strings: non-parameterized are `public val <accessor>: String` spread across
# lines with a later `get() = stringResource(Res.string.<resource>)`.
# Parameterized are a single-line `public fun <accessor>(...): String =
# stringResource(Res.string.<resource>, ...)`.
# shellcheck disable=SC2016
check_catalog "Strings" "string" "$STRINGS_FILE" '
  match($0, /public fun ([A-Za-z_][A-Za-z0-9_]*)\(.*Res\.string\.([A-Za-z_][A-Za-z0-9_]*)/, m) {
    print m[1], m[2]; acc = ""; next
  }
  match($0, /public val ([A-Za-z_][A-Za-z0-9_]*):/, m) { acc = m[1]; next }
  acc != "" && match($0, /Res\.string\.([A-Za-z_][A-Za-z0-9_]*)/, m) {
    print acc, m[1]; acc = ""
  }
'

# Plurals: both accessor and resource on the same line
#   `public fun <accessor>(...): String = pluralStringResource(Res.plurals.<resource>, ...)`
# shellcheck disable=SC2016
check_catalog "Plurals" "plurals" "$PLURALS_FILE" '
  match($0, /public fun ([A-Za-z_][A-Za-z0-9_]*)\(.*Res\.plurals\.([A-Za-z_][A-Za-z0-9_]*)/, m) {
    print m[1], m[2]
  }
'

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
