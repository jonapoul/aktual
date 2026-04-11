#!/usr/bin/env bash
#
# Checks for new database migrations in the upstream actualbudget/actual repo.
# If new migrations are found, creates or updates a PR with the updated tracker file
# and lists the new migrations in the PR body.
#
# Usage:
#   # Dry run — prints what would happen without touching GitHub
#   DRY_RUN=1 ./check-migrations.sh
#
#   # Full run — creates/updates a PR in the given repo (requires gh CLI auth)
#   GITHUB_REPOSITORY=owner/repo ./check-migrations.sh
#
# Environment variables:
#   GITHUB_REPOSITORY  Required for full runs. The owner/repo to create the PR in.
#   DRY_RUN            Set to any value to skip GitHub operations and just print results.
#   GITHUB_TOKEN       Used by gh CLI for authentication (set automatically in GitHub Actions).
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TRACKER_FILE="$SCRIPT_DIR/last-known-migration.txt"
UPSTREAM_REPO="https://github.com/actualbudget/actual.git"
MIGRATIONS_PATH="packages/loot-core/migrations"
PR_LABEL="upstream-changes"
PR_BRANCH="auto/upstream-migrations"

DRY_RUN="${DRY_RUN:-}"
GITHUB_REPOSITORY="${GITHUB_REPOSITORY:-}"

last_known="$(tr -d '[:space:]' < "$TRACKER_FILE")"
echo "Last known migration: $last_known"

tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT

echo "Cloning upstream migrations (sparse checkout)..."
git clone --depth 1 --filter=blob:none --sparse --quiet "$UPSTREAM_REPO" "$tmp_dir"
git -C "$tmp_dir" sparse-checkout set "$MIGRATIONS_PATH"

mapfile -t all_migrations < <(find "$tmp_dir/$MIGRATIONS_PATH" -maxdepth 1 -type f -printf '%f\n' | sort)

new_migrations=()
found_last=false
for migration in "${all_migrations[@]}"; do
  if [[ "$found_last" == true ]]; then
    new_migrations+=("$migration")
  elif [[ "$migration" == "$last_known" ]]; then
    found_last=true
  fi
done

if [[ "$found_last" == false ]]; then
  echo "WARNING: Last known migration '$last_known' was not found in upstream."
  echo "This may mean it was renamed or removed. Listing all migrations after it alphabetically."
  for migration in "${all_migrations[@]}"; do
    if [[ "$migration" > "$last_known" ]]; then
      new_migrations+=("$migration")
    fi
  done
fi

if [[ ${#new_migrations[@]} -eq 0 ]]; then
  echo "No new migrations found."
  exit 0
fi

echo "Found ${#new_migrations[@]} new migration(s):"
printf '  %s\n' "${new_migrations[@]}"

build_table_rows() {
  for migration in "$@"; do
    local ext="${migration##*.}"
    local type
    case "$ext" in
      sql) type="SQL" ;;
      js)  type="JavaScript" ;;
      *)   type="$ext" ;;
    esac
    echo "| [\`$migration\`](https://github.com/actualbudget/actual/blob/master/$MIGRATIONS_PATH/$migration) | $type |"
  done
}

new_rows="$(build_table_rows "${new_migrations[@]}")"

newest_migration="${new_migrations[${#new_migrations[@]}-1]}"

if [[ -n "$DRY_RUN" ]]; then
  echo ""
  echo "=== DRY RUN ==="
  echo "Would update tracker to: $newest_migration"
  echo "New table rows:"
  echo "$new_rows"
  exit 0
fi

if [[ -z "$GITHUB_REPOSITORY" ]]; then
  echo "ERROR: GITHUB_REPOSITORY is not set. Set it or use DRY_RUN=1." >&2
  exit 1
fi

# Update the tracker file to the newest migration
echo "$newest_migration" > "$TRACKER_FILE"

# Set up git for committing
git config user.name "Jon Poulton"
git config user.email "jpoulton@pm.me"

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
  echo "$newest_migration" > "$TRACKER_FILE"
  git add "$TRACKER_FILE"
  git commit -m "Update last known upstream migration to $newest_migration"
  git push origin "$PR_BRANCH"

  # Extract existing table rows from the PR body and append new ones
  existing_rows="$(echo "$existing_body" | sed -n '/^| \[/p')"
  all_rows="${existing_rows}"$'\n'"${new_rows}"

  updated_body="$(cat <<EOF
## New Upstream Migrations

The following database migrations have been added to [actualbudget/actual](https://github.com/actualbudget/actual) and may need corresponding changes in Aktual.

| Migration | Type |
|-----------|------|
${all_rows}

[View migrations directory](https://github.com/actualbudget/actual/tree/master/packages/loot-core/migrations)
EOF
)"

  gh pr edit "$pr_number" \
    --repo "$GITHUB_REPOSITORY" \
    --body "$updated_body"

  echo "PR #$pr_number updated."
else
  echo "Creating new PR..."

  body="$(cat <<EOF
## New Upstream Migrations

The following database migrations have been added to [actualbudget/actual](https://github.com/actualbudget/actual) and may need corresponding changes in Aktual.

| Migration | Type |
|-----------|------|
${new_rows}

[View migrations directory](https://github.com/actualbudget/actual/tree/master/packages/loot-core/migrations)
EOF
)"

  # Clean up any stale remote branch (e.g. from a previous failed run)
  git push origin --delete "$PR_BRANCH" 2>/dev/null || true

  # Create branch, commit, and push
  git checkout -B "$PR_BRANCH"
  git add "$TRACKER_FILE"
  git commit -m "Update last known upstream migration to $newest_migration"
  git push -u origin "$PR_BRANCH"

  gh pr create \
    --repo "$GITHUB_REPOSITORY" \
    --title "Update database" \
    --body "$body" \
    --label "$PR_LABEL"

  echo "PR created."
fi

echo "Tracker updated to: $newest_migration"
