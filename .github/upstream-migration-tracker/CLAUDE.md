# Upstream Migration Tracker

Automated system that checks daily for new database migrations in [actualbudget/actual](https://github.com/actualbudget/actual) and opens a PR when new ones are found.

## How It Works

1. The GitHub Action (`.github/workflows/check-upstream-migrations.yml`) runs daily at 08:00 UTC and can be triggered manually.
2. `check-migrations.sh` sparse-clones the upstream repo and compares migration files against `last-known-migration.txt`.
3. If new migrations are found:
   - Updates `last-known-migration.txt` to the newest migration.
   - Creates a PR on branch `auto/upstream-migrations` listing the new migrations (label: `db-migration`).
   - If a PR already exists, pushes to the same branch and appends the new migrations to the PR body.
4. If no new migrations are found, the workflow exits cleanly with no side effects.

## Files

- `last-known-migration.txt` — single-line file containing the filename of the most recent upstream migration that has been implemented in Aktual. Updated automatically by the script.
- `check-migrations.sh` — core detection and PR logic. Run with `DRY_RUN=1` for local testing.

## Upstream Migrations Location

Migrations live at `packages/loot-core/migrations/` in the upstream repo. Files are named `<unix_timestamp>_<description>.<sql|js>`.
