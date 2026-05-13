package aktual.budget.db

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import logcat.logcat

// Intentionally not calling BudgetDatabase.Schema.migrate because upstream Actual's database
// doesn't use the user_version SQLite property, so it won't work in sync with SQLDelight.
fun migrateDatabase(driver: SqlDriver, db: BudgetDatabase) = QueryResult.AsyncValue {
  val queries = db.migrationsQueries
  val previousMigrations = queries.getAll().executeAsList()
  val migrator = Migrator(driver, queries, previousMigrations)

  with(migrator) {
    // packages/loot-core/migrations/1769000000000_add_custom_upcoming_length.sql
    migrateTo(version = 1769000000000L) {
      "ALTER TABLE schedules ADD COLUMN custom_upcoming_length TEXT DEFAULT NULL"
    }

    // packages/loot-core/migrations/1778510362740_add_cleanup_groups_and_def.sql
    migrateTo(version = 1778510362740L) {
      """
        ALTER TABLE categories ADD COLUMN cleanup_def TEXT DEFAULT NULL;

        CREATE TABLE cleanup_groups
          (id TEXT PRIMARY KEY,
           name TEXT NOT NULL,
           tombstone INTEGER DEFAULT 0);
      """.trimIndent()
    }
  }
}

private class Migrator(
  private val driver: SqlDriver,
  private val migrationsQueries: MigrationsQueries,
  private val previousMigrations: List<Long>,
) {
  suspend fun migrateTo(version: Long, sql: () -> String) {
    if (version !in previousMigrations) {
      logcat.i { "Migrating DB to version $version" }

      // Actually run the migration
      driver.execute(identifier = null, sql = sql(), parameters = 0).await()
      // Update the migrations table
      migrationsQueries.withoutResult { insert(version) }
    }
  }
}
