# aktual-budget:data:db

SQLDelight database module. Owns the schema, adapters, and client-side migration logic.

## Adding a new upstream migration

When `.github/upstream-migration-tracker/last-known-migration.txt` is updated with a new migration, run the `/apply-upstream-migration` skill. It covers:

1. Reading the upstream SQL from `/home/jon/dev/actual/packages/loot-core/migrations/`
2. Updating the SQLDelight `.sq` files (new columns / new tables)
3. Adding model types and ID value classes in `aktual-budget:model`
4. Wiring adapters in `Adapters.kt` and `BuildDatabase.kt`
5. Adding the migration function to `MigrateDatabase.kt`
6. Adding a check to `LoadExistingDatabaseFromFileTest`

## Key files

| File | Purpose |
|------|---------|
| `MigrateDatabase.kt` | Custom migration runner — one `migrateXXX()` extension function per upstream migration (XXX = numeric timestamp) |
| `Adapters.kt` | Column type adapters (JSON, typed IDs, enums) |
| `BuildDatabase.kt` | Constructs `BudgetDatabase` with all adapters |
| `sqldelight/aktual/budget/db/*.sq` | Table definitions and named queries |
| `sqldelight/schemas/1.db` | Schema snapshot for fresh installs (regenerated on compile) |

## Migration rules

- Use `CREATE TABLE IF NOT EXISTS` — fresh databases already have the table from the schema
- `ALTER TABLE ... ADD COLUMN` has no `IF NOT EXISTS` — the `version !in previousMigrations` guard handles idempotency
- Never call `BudgetDatabase.Schema.migrate()` — upstream Actual doesn't use SQLite's `user_version` pragma
