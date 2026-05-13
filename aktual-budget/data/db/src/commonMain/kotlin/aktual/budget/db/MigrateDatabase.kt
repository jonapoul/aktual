package aktual.budget.db

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.db.SqlDriver
import logcat.logcat

// Intentionally not calling BudgetDatabase.Schema.migrate because upstream Actual's database
// doesn't use the user_version SQLite property, so it won't work in sync with SQLDelight.
suspend fun migrateDatabase(driver: SqlDriver, db: BudgetDatabase) {
  val previousMigrations = db.migrationsQueries.getAll().awaitAsList()

  for ((version, statements) in DatabaseMigrations) {
    if (version !in previousMigrations) {
      logcat.i(TAG) { "Migrating DB to version $version" }
      for (statement in statements) {
        driver.execute(identifier = null, sql = statement, parameters = 0).await()
      }
    }
  }
}

// Sorted list of upstream migration version → SQL statements to apply. Also used by
// SqlDriverFactory.onCreate to seed __migrations__ on fresh installs, so that migrateDatabase skips
// columns/tables that the schema already created.
internal val DatabaseMigrations: List<Pair<Long, List<String>>> =
  listOf(
    // packages/loot-core/migrations/1769000000000_add_custom_upcoming_length.sql
    1769000000000L to
      listOf("ALTER TABLE schedules ADD COLUMN custom_upcoming_length TEXT DEFAULT NULL"),

    // packages/loot-core/migrations/1778510362740_add_cleanup_groups_and_def.sql
    1778510362740L to
      listOf(
        "ALTER TABLE categories ADD COLUMN cleanup_def TEXT DEFAULT NULL",
        """
        CREATE TABLE IF NOT EXISTS cleanup_groups(
          id TEXT PRIMARY KEY,
          name TEXT NOT NULL,
          tombstone INTEGER DEFAULT 0
        )
        """
          .trimIndent(),
      ),
  )

private const val TAG = "MigrateDatabase"
