#!/usr/bin/env bash
#
# Checks for new theme attributes in the upstream actualbudget/actual repo.
# If new attributes are found, creates or updates a PR with the updated tracker file
# and lists the new attributes in the PR body.
#
# Monitored files:
#   - packages/component-library/src/theme.ts        (theme contract / CSS variable names)
#   - packages/desktop-client/src/style/themes/*.ts   (dark, light, midnight color values)
#
# Usage:
#   # Dry run — prints what would happen without touching GitHub
#   DRY_RUN=1 ./check-theme-attributes.sh
#
#   # Full run — creates/updates a PR in the given repo (requires gh CLI auth)
#   GITHUB_REPOSITORY=owner/repo ./check-theme-attributes.sh
#
# Environment variables:
#   GITHUB_REPOSITORY  Required for full runs. The owner/repo to create the PR in.
#   DRY_RUN            Set to any value to skip GitHub operations and just print results.
#   GITHUB_TOKEN       Used by gh CLI for authentication (set automatically in GitHub Actions).
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TRACKER_FILE="$SCRIPT_DIR/last-known-theme-attributes.txt"
UPSTREAM_REPO="https://github.com/actualbudget/actual.git"
THEME_CONTRACT_DIR="packages/component-library/src"
THEME_CONTRACT_FILE="packages/component-library/src/theme.ts"
THEME_DIR="packages/desktop-client/src/style/themes"
PR_LABEL="theme-attributes"
PR_BRANCH="auto/upstream-theme-attributes"

DRY_RUN="${DRY_RUN:-}"
GITHUB_REPOSITORY="${GITHUB_REPOSITORY:-}"

echo "Loading known theme attributes from tracker..."
known_attrs="$(sort < "$TRACKER_FILE")"
known_count="$(echo "$known_attrs" | wc -l)"
echo "Known attributes: $known_count"

tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT

echo "Cloning upstream theme files (sparse checkout)..."
git clone --depth 1 --filter=blob:none --sparse --quiet "$UPSTREAM_REPO" "$tmp_dir"
git -C "$tmp_dir" sparse-checkout set "$THEME_CONTRACT_DIR" "$THEME_DIR"

# Extract attribute names from theme.ts (object keys like "  someKey: 'var(...)'")
grep -oP '^\s+(\w+)\s*:' "$tmp_dir/$THEME_CONTRACT_FILE" \
  | sed 's/[: ]//g' \
  | sort -u > "$tmp_dir/contract_attrs.txt"

# Extract export names from each theme file (lines like "export const someName = ...")
for theme_file in "$tmp_dir/$THEME_DIR"/*.ts; do
  grep -oP '^export const (\w+)' "$theme_file" | sed 's/export const //'
done | sort -u > "$tmp_dir/theme_file_attrs.txt"

# Union of all attributes
sort -u "$tmp_dir/contract_attrs.txt" "$tmp_dir/theme_file_attrs.txt" > "$tmp_dir/all_attrs.txt"

# Find new attributes (in upstream but not in our tracker)
new_attrs="$(comm -23 "$tmp_dir/all_attrs.txt" <(echo "$known_attrs"))"

# Also find removed attributes (in our tracker but not upstream)
removed_attrs="$(comm -13 "$tmp_dir/all_attrs.txt" <(echo "$known_attrs"))"

if [[ -z "$new_attrs" && -z "$removed_attrs" ]]; then
  echo "No theme attribute changes found."
  exit 0
fi

if [[ -n "$new_attrs" ]]; then
  new_count="$(echo "$new_attrs" | wc -l)"
  echo "Found $new_count new attribute(s):"
  echo "  ${new_attrs//$'\n'/$'\n'  }"
fi

if [[ -n "$removed_attrs" ]]; then
  removed_count="$(echo "$removed_attrs" | wc -l)"
  echo "Found $removed_count removed attribute(s):"
  echo "  ${removed_attrs//$'\n'/$'\n'  }"
fi

# Build table rows for new attributes, noting which file(s) they appear in
build_new_rows() {
  while IFS= read -r attr; do
    sources=()
    if grep -qx "$attr" "$tmp_dir/contract_attrs.txt"; then
      sources+=("theme.ts")
    fi
    for theme_file in "$tmp_dir/$THEME_DIR"/*.ts; do
      basename="$(basename "$theme_file")"
      if grep -qP "^export const ${attr}\b" "$theme_file"; then
        sources+=("$basename")
      fi
    done
    source_str="$(printf '%s\n' "${sources[@]}" | paste -sd',' | sed 's/,/, /g')"
    echo "| \`$attr\` | $source_str |"
  done <<< "$1"
}

build_removed_rows() {
  while IFS= read -r attr; do
    echo "| \`$attr\` |"
  done <<< "$1"
}

new_rows=""
if [[ -n "$new_attrs" ]]; then
  new_rows="$(build_new_rows "$new_attrs")"
fi

removed_rows=""
if [[ -n "$removed_attrs" ]]; then
  removed_rows="$(build_removed_rows "$removed_attrs")"
fi

if [[ -n "$DRY_RUN" ]]; then
  echo ""
  echo "=== DRY RUN ==="
  if [[ -n "$new_rows" ]]; then
    echo "New attribute rows:"
    echo "$new_rows"
  fi
  if [[ -n "$removed_rows" ]]; then
    echo "Removed attribute rows:"
    echo "$removed_rows"
  fi
  exit 0
fi

if [[ -z "$GITHUB_REPOSITORY" ]]; then
  echo "ERROR: GITHUB_REPOSITORY is not set. Set it or use DRY_RUN=1." >&2
  exit 1
fi

# Update the tracker file to the current upstream state
cp "$tmp_dir/all_attrs.txt" "$TRACKER_FILE"

# Set up git for committing
git config user.name "Jon Poulton"
git config user.email "jpoulton@pm.me"

# Build the PR body
build_body() {
  cat <<'HEADER'
## New Upstream Theme Attributes

The following theme attribute changes have been detected in [actualbudget/actual](https://github.com/actualbudget/actual) and may need corresponding changes in Aktual's `Theme.kt` and theme implementations.

HEADER

  if [[ -n "$new_rows" ]]; then
    cat <<NEWTABLE
### Added

| Attribute | Source File(s) |
|-----------|----------------|
${new_rows}

NEWTABLE
  fi

  if [[ -n "$removed_rows" ]]; then
    cat <<REMOVEDTABLE
### Removed

| Attribute |
|-----------|
${removed_rows}

REMOVEDTABLE
  fi

  cat <<'FOOTER'
### Upstream Files

- [`theme.ts`](https://github.com/actualbudget/actual/blob/master/packages/component-library/src/theme.ts) (theme contract)
- [`themes/`](https://github.com/actualbudget/actual/tree/master/packages/desktop-client/src/style/themes) (dark, light, midnight)
FOOTER
}

body="$(build_body)"

# Check for an existing open PR
existing_pr="$(gh pr list \
  --repo "$GITHUB_REPOSITORY" \
  --label "$PR_LABEL" \
  --state open \
  --limit 1 \
  --json number,body \
  --jq '.[0] // empty')"

if [[ -n "$existing_pr" ]]; then
  pr_number="$(echo "$existing_pr" | jq -r '.number')"
  existing_body="$(echo "$existing_pr" | jq -r '.body')"
  echo "Updating existing PR #$pr_number..."

  # Check out the existing PR branch and update the tracker file
  git fetch origin "$PR_BRANCH"
  git checkout "$PR_BRANCH"
  cp "$tmp_dir/all_attrs.txt" "$TRACKER_FILE"
  git add "$TRACKER_FILE"
  git commit -m "Update known upstream theme attributes"
  git push origin "$PR_BRANCH"

  # Merge existing and new table rows
  merge_rows() {
    local section="$1"
    local new="$2"
    # Extract existing rows from the PR body for this section
    local existing
    existing="$(echo "$existing_body" | sed -n "/^### ${section}/,/^###/p" | sed -n '/^| `/p')" || true
    if [[ -n "$existing" && -n "$new" ]]; then
      # Combine, deduplicate by attribute name
      { echo "$existing"; echo "$new"; } | sort -t'|' -k2 -u
    elif [[ -n "$new" ]]; then
      echo "$new"
    elif [[ -n "$existing" ]]; then
      echo "$existing"
    fi
  }

  merged_new_rows="$(merge_rows "Added" "$new_rows")"
  merged_removed_rows="$(merge_rows "Removed" "$removed_rows")"

  # Reassign for body rebuild
  new_rows="$merged_new_rows"
  removed_rows="$merged_removed_rows"
  body="$(build_body)"

  gh pr edit "$pr_number" \
    --repo "$GITHUB_REPOSITORY" \
    --body "$body"

  echo "PR #$pr_number updated."
else
  echo "Creating new PR..."

  # Clean up any stale remote branch
  git push origin --delete "$PR_BRANCH" 2>/dev/null || true

  # Create branch, commit, and push
  git checkout -B "$PR_BRANCH"
  git add "$TRACKER_FILE"
  git commit -m "Update known upstream theme attributes"
  git push -u origin "$PR_BRANCH"

  gh pr create \
    --repo "$GITHUB_REPOSITORY" \
    --title "Update themes" \
    --body "$body" \
    --label "$PR_LABEL"

  echo "PR created."
fi

echo "Tracker updated."
