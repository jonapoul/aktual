# aktual-budget:data:db

SQLDelight database module. Owns the schema, adapters and DAOs. The migration runner (`MigrateDatabase.kt`) and `AndroidxSqlDriverFactory` live in `aktual-budget:data:impl`, so `data:db` doesn't depend on `budget:utils` or `aktual-logging`. Keep it that way: domain modules depend on `data:db` through `api`.

## Adding a new upstream migration

When `.github/upstream-migration-tracker/last-known-migration.txt` is updated with a new migration, run the `/apply-upstream-migration` skill. It covers:

1. Reading the upstream SQL from `/home/jon/dev/actual/packages/loot-core/migrations/`
2. Updating the SQLDelight `.sq` files (new columns / new tables)
3. Running `gradle generateDatabaseSchema`
4. Adding model types and ID value classes in `aktual-budget:model`
5. Wiring adapters in `Adapters.kt` and `BuildDatabase.kt`
6. Adding the migration function to `MigrateDatabase.kt` (in `data:impl`)
7. Adding a check to `DatabaseMigrationTest` (in `data:impl`)

## Key files

| File | Purpose |
|------|---------|
| `MigrateDatabase.kt` (`data:impl`) | Custom migration runner - `DatabaseMigrations` list maps `Long` timestamp → `List<String>` of SQL statements |
| `Adapters.kt` | Column type adapters (JSON, typed IDs, enums) |
| `RuleJson.kt` | Serializers for rule conditions/actions that map upstream's internal field names (see below) |
| `BuildDatabase.kt` | Constructs `BudgetDatabase` with all adapters |
| `sqldelight/aktual/budget/db/*.sq` | Table definitions and named queries |
| `sqldelight/schemas/1.db` | Schema snapshot for fresh installs (regenerated on compile) |

## Rule field names

Upstream stores the `conditions` and `actions` of the `rules` table with the `transactions` table's column names where they differ from the public ones: `payee` as `description`, `account` as `acct`, `imported_payee` as `imported_description` (`toInternalField` / `fromInternalField` in `loot-core/src/server/transactions/transaction-rules.ts`). Its SQL, like the payee rules lookup in `Payees.sq`, relies on the internal names.

`RuleJson.conditions` / `RuleJson.actions` do the same renaming. The `rules` table adapter uses them, so loaded rules only use `Payee` / `Account` / `ImportedPayee`. Anything writing rule JSON to a sync message (`localChange(RULES, ...)`) must encode with them too, not with a plain `ListSerializer`. Saved filters and custom reports store conditions with the public names, so they don't use them.

## Migration rules

- Every `DatabaseMigrations` entry mirrors an upstream migration file except the ones flagged "NOT an upstream migration" (Aktual's own views, e.g. `v_schedule_recurring`). Existing databases never re-run `Schema.create`, so a new table or view needs an entry here or it won't exist on them
- Use `CREATE TABLE IF NOT EXISTS` - fresh databases already have the table from the schema
- `ALTER TABLE ... ADD COLUMN` has no `IF NOT EXISTS`. Two things keep it idempotent: the `version !in previousMigrations` guard (skips versions already recorded in `__migrations__`), and a `PRAGMA table_info` pre-check in `execute()` that skips the `ALTER` when the column already exists. The pre-check matters because `Schema.create` runs on every Actual DB we open (they all report `user_version` 0) and backfills any table missing from the file with the *full current schema* - so a column on such a table (e.g. `tags.hidden` on a pre-tags DB) is already present before migrations run. Don't try to catch the duplicate-column error instead: on the androidx host-test target the exception reaches the catch as a message-less `android.database.SQLException` (coroutines stacktrace recovery strips it), so message matching is unreliable
- Each migration's statements and version insert run in a single `db.transaction { }`, so they're atomic
- A fresh database (`AndroidxSqlDriverFactory.onCreate`) records `PreAktualMigrations` plus every `DatabaseMigrations` version, since upstream refuses to open a file missing any of its migrations. New budgets (`BudgetCreatorImpl`) rely on this
- Never call `BudgetDatabase.Schema.migrate()` - upstream Actual doesn't use SQLite's `user_version` pragma
