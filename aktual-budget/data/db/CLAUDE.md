# aktual-budget:data:db

SQLDelight database module. Owns the schema, adapters, and client-side migration logic.

## Adding a new upstream migration

When `.github/upstream-migration-tracker/last-known-migration.txt` is updated with a new migration, run the `/apply-upstream-migration` skill. It covers:

1. Reading the upstream SQL from `/home/jon/dev/actual/packages/loot-core/migrations/`
2. Updating the SQLDelight `.sq` files (new columns / new tables)
3. Running `gradle generateDatabaseSchema`
4. Adding model types and ID value classes in `aktual-budget:model`
5. Wiring adapters in `Adapters.kt` and `BuildDatabase.kt`
6. Adding the migration function to `MigrateDatabase.kt`
7. Adding a check to `LoadExistingDatabaseFromFileTest`

## Key files

| File | Purpose |
|------|---------|
| `MigrateDatabase.kt` | Custom migration runner — `DatabaseMigrations` list maps `Long` timestamp → `List<String>` of SQL statements |
| `Adapters.kt` | Column type adapters (JSON, typed IDs, enums) |
| `BuildDatabase.kt` | Constructs `BudgetDatabase` with all adapters |
| `sqldelight/aktual/budget/db/*.sq` | Table definitions and named queries |
| `sqldelight/schemas/1.db` | Schema snapshot for fresh installs (regenerated on compile) |

## Migration rules

- Use `CREATE TABLE IF NOT EXISTS` — fresh databases already have the table from the schema
- `ALTER TABLE ... ADD COLUMN` has no `IF NOT EXISTS`. Two things keep it idempotent: the `version !in previousMigrations` guard (skips versions already recorded in `__migrations__`), and a `PRAGMA table_info` pre-check in `execute()` that skips the `ALTER` when the column already exists. The pre-check matters because `Schema.create` runs on every Actual DB we open (they all report `user_version` 0) and backfills any table missing from the file with the *full current schema* — so a column on such a table (e.g. `tags.hidden` on a pre-tags DB) is already present before migrations run. Don't try to catch the duplicate-column error instead: on the androidx host-test target the exception reaches the catch as a message-less `android.database.SQLException` (coroutines stacktrace recovery strips it), so message matching is unreliable
- Each migration's statements and version insert run in a single `db.transaction { }`, so they're atomic
- Never call `BudgetDatabase.Schema.migrate()` — upstream Actual doesn't use SQLite's `user_version` pragma
