---
name: apply-upstream-migration
description: Apply a new upstream Actual migration to Aktual — update MigrateDatabase.kt, SQLDelight .sq files, Adapters, model types, and the migration test. Triggered when last-known-migration.txt is updated.
argument-hint: "<migration-filename>"
---

Apply a new upstream Actual database migration to Aktual's database layer.

Do NOT ask clarifying questions — just execute.

## Arguments

- `$ARGUMENTS` is the migration filename, e.g. `1778510362740_add_cleanup_groups_and_def.sql`
- If omitted, read it from `.github/upstream-migration-tracker/last-known-migration.txt`

## Step-by-Step Process

### 1. Read the upstream migration SQL

The migration lives at:
```
/home/jon/dev/actual/packages/loot-core/migrations/<filename>
```

Parse all DDL statements: `ALTER TABLE ... ADD COLUMN`, `CREATE TABLE`, `CREATE INDEX`, etc. Strip `BEGIN TRANSACTION` / `COMMIT` wrappers.

### 2. Categorise the changes

For each statement:

| Statement | Actions needed |
|-----------|---------------|
| `ALTER TABLE t ADD COLUMN c` | Update `.sq` file + possible adapter |
| `CREATE TABLE t` | New `.sq` file + adapter + `BuildDatabase.kt` + possible ID type |
| `CREATE INDEX` | Add to the relevant `.sq` file (no adapter) |

Also check if new column types need new model classes (sealed interfaces, value class IDs).

### 3. Update SQLDelight schema files

All `.sq` files: `aktual-budget/data/db/src/commonMain/sqldelight/aktual/budget/db/`

**New column on an existing table:**
- Add the column to the `CREATE TABLE` statement in the matching `.sq` file
- If it stores typed JSON, declare it as `TEXT AS MyType DEFAULT NULL` with an import at the top
- If it stores a typed ID, declare it as `TEXT AS FooId DEFAULT NULL`

**New table:**
- Create `Foo.sq` (PascalCase, matching the table name)
- Mirror upstream columns with SQLDelight typed aliases
- Use `tombstone INTEGER AS Boolean DEFAULT 0` for the tombstone column
- Add at minimum: `insert: INSERT OR REPLACE INTO foo(id, ...) VALUES (?, ...);` and `getById: SELECT * FROM foo WHERE id = ?;`

### 4. Add model types if needed

Types live in `aktual-budget/model/src/commonMain/kotlin/aktual/budget/model/`.

**New table ID** — add to `Ids.kt`, following the existing value class pattern:
```kotlin
@JvmInline
@Serializable
value class FooId(val value: String) : Comparable<FooId> {
  override fun toString(): String = value
  override fun compareTo(other: FooId) = value.compareTo(other.value)
}
```

**New JSON column type** — check the upstream TypeScript type at `actual/packages/loot-core/src/types/models/` in the parent repo, then create a new file:
- Discriminated union → `sealed interface` + `JsonContentPolymorphicSerializer` (see `CleanupTemplate.kt` as a reference)
- Plain object → `@Serializable data class`
- Use typed IDs (e.g. `FooId`) for any ID fields, not raw `String`

### 5. Update Adapters.kt

File: `aktual-budget/data/db/src/commonMain/kotlin/aktual/budget/db/Adapters.kt`

- New JSON column (single): `private val foo = jsonSerializable(Foo.serializer())`
- New JSON column (array): `private val foos = jsonSerializable(ListSerializer(Foo.serializer()))`
- New ID column: `private val fooId = stringAdapter(::FooId)`
- Wire the adapter into the relevant `internal val FooAdapter = Foo.Adapter(...)` block, or create a new `internal val` if a new table was added

### 6. Update BuildDatabase.kt

File: `aktual-budget/data/db/src/commonMain/kotlin/aktual/budget/db/BuildDatabase.kt`

Pass any new table adapter into `BudgetDatabase(...)`. The parameter name is the snake_case table name with `Adapter` appended directly (no extra underscore):
```kotlin
fooAdapter = FooAdapter,           // single-word table: foo
foo_barsAdapter = FooBarsAdapter,  // multi-word table: foo_bars
```

### 7. Add the migration function in MigrateDatabase.kt

File: `aktual-budget/data/db/src/commonMain/kotlin/aktual/budget/db/MigrateDatabase.kt`

Add the call inside `with(migrator) { ... }` in `migrateDatabase`:
```kotlin
migrate1778510362740()
```
(Name = `migrate` + the numeric version from the filename.)

Add the function at the bottom of the file:
```kotlin
// packages/loot-core/migrations/1778510362740_add_cleanup_groups_and_def.sql
private suspend fun Migrator.migrate1778510362740() =
  migrateTo(
    version = 1778510362740L,
    "ALTER TABLE categories ADD COLUMN cleanup_def TEXT DEFAULT NULL",
    "CREATE TABLE IF NOT EXISTS cleanup_groups(id TEXT PRIMARY KEY, name TEXT NOT NULL, tombstone INTEGER DEFAULT 0)",
  )
```

Rules:
- **One String per DDL statement** — `driver.execute()` doesn't support semicolon-separated multi-statement strings
- **`CREATE TABLE IF NOT EXISTS`** — always use; fresh databases already have the table from the schema
- **`ALTER TABLE ... ADD COLUMN`** — use as-is; the `version !in previousMigrations` guard handles idempotency for existing databases
- Never call `BudgetDatabase.Schema.migrate()` — upstream doesn't use `user_version`

### 8. Update the migration test

File: `aktual-budget/data/db/src/commonTest/kotlin/aktual/budget/db/DatabaseMigrationTest.kt`

Add a call at the end of the test body:
```kotlin
checkMigration1778510362740(db)
```
(Name = `checkMigration` + the numeric version.)

Add the check function. The query must reference the new column or table so it fails to compile if the schema change wasn't applied.

**New table** — query by ID, expect null:
```kotlin
// Adds cleanup_groups table
private suspend fun checkMigration1778510362740(db: BudgetDatabase) {
  val result = db.cleanupGroupsQueries.getById(CleanupGroupId("nonexistent")).awaitAsOneOrNull()
  assertNull(result)
}
```

**New column on an existing table** — use (or add) a query that selects that column:
```kotlin
// Adds custom_upcoming_length column to schedules
private suspend fun checkMigration1769000000000(db: BudgetDatabase) {
  val result = db.schedulesQueries.getCustomUpcomingLength(ScheduleId("nonexistent")).awaitAsOneOrNull()
  assertNull(result)
}
```
Add a named query to the `.sq` file if one doesn't already return the new column.

### 9. Compile and test

```bash
./gradlew :aktual-budget:data:db:compileAll
./gradlew :aktual-budget:data:db:testAndroidHostTest
```

Fix any adapter or schema errors before declaring done.

## Important Notes

- Always check the upstream TypeScript types when adding a JSON column — the Kotlin model must match the serialised shape exactly
- `schemas/1.db` is the SQLDelight schema snapshot used for fresh database creation — it is regenerated automatically when SQLDelight compiles the `.sq` files
- The `__migrations__` table tracks which Aktual migrations have run; upstream Actual's own migration tracking is completely separate
- The migration SQL runs against **existing** Actual databases opened from file — fresh databases already contain all tables/columns from the schema, hence `CREATE TABLE IF NOT EXISTS`
