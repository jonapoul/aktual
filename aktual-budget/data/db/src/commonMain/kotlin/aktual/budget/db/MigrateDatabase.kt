package aktual.budget.db

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import logcat.logcat

// Intentionally not calling BudgetDatabase.Schema.migrate because upstream Actual's database
// doesn't use the user_version SQLite property, so it won't work in sync with SQLDelight.
fun migrateDatabase(driver: SqlDriver, db: BudgetDatabase) = QueryResult.AsyncValue {
  val queries = db.migrationsQueries
  val previousMigrations = queries.getAll().awaitAsList()
  val migrator = Migrator(driver, queries, previousMigrations)

  with(migrator) {
    migrate1769000000000()
    migrate1778510362740()
  }
}

private class Migrator(
  private val driver: SqlDriver,
  private val migrationsQueries: MigrationsQueries,
  private val previousMigrations: List<Long>,
) {
  // Each string in sql is executed as a separate statement — Android's execSQL doesn't support
  // multiple semicolon-separated statements in one call.
  suspend fun migrateTo(version: Long, vararg sql: String) {
    if (version !in previousMigrations) {
      logcat.i { "Migrating DB to version $version" }
      for (statement in sql) {
        driver.execute(identifier = null, sql = statement, parameters = 0).await()
      }
      migrationsQueries.withoutResult { insert(version) }
    }
  }
}

// packages/loot-core/migrations/1769000000000_add_custom_upcoming_length.sql
private suspend fun Migrator.migrate1769000000000() =
  migrateTo(
    version = 1769000000000L,
    "ALTER TABLE schedules ADD COLUMN custom_upcoming_length TEXT DEFAULT NULL",
  )

// packages/loot-core/migrations/1778510362740_add_cleanup_groups_and_def.sql
private suspend fun Migrator.migrate1778510362740() =
  migrateTo(
    version = 1778510362740L,
    "ALTER TABLE categories ADD COLUMN cleanup_def TEXT DEFAULT NULL",
    "CREATE TABLE IF NOT EXISTS cleanup_groups(id TEXT PRIMARY KEY, name TEXT NOT NULL, tombstone INTEGER DEFAULT 0)",
  )
