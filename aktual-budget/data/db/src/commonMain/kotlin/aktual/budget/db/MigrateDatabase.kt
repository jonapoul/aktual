package aktual.budget.db

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import logcat.logcat

// Intentionally not calling BudgetDatabase.Schema.migrate because upstream Actual's database
// doesn't use the user_version SQLite property, so it won't work in sync with SQLDelight.
suspend fun migrateDatabase(driver: SqlDriver, db: BudgetDatabase) {
  val previousMigrations = db.migrationsQueries.getAll().awaitAsList()

  for ((version, statements) in DatabaseMigrations) {
    if (version !in previousMigrations) {
      logcat.i(TAG) { "Migrating DB to version $version" }
      db.transaction {
        for (statement in statements) {
          execute(driver, statement)
        }
        db.migrationsQueries.insert(version)
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

    // packages/loot-core/migrations/1780099200000_add_show_trend_lines_report_setting.sql
    1780099200000L to
      listOf("ALTER TABLE custom_reports ADD COLUMN show_trend_lines INTEGER DEFAULT 0"),

    // packages/loot-core/migrations/1780327681000_add_tags_hidden.sql
    1780327681000L to listOf("ALTER TABLE tags ADD COLUMN hidden INTEGER DEFAULT 0"),

    // packages/loot-core/migrations/1780606215000_add_bank_sync_status.sql
    1780606215000L to listOf("ALTER TABLE accounts ADD COLUMN bank_sync_status TEXT DEFAULT NULL"),

    // packages/loot-core/migrations/1780606215001_add_performance_indexes.sql
    1780606215001L to
      listOf(
        "CREATE INDEX IF NOT EXISTS idx_transactions_acct_tombstone ON transactions(acct, tombstone)",
        "CREATE INDEX IF NOT EXISTS idx_transactions_schedule ON transactions(schedule)",
      ),
  )

private const val TAG = "MigrateDatabase"

// Keep ADD COLUMN migrations idempotent. Schema.create runs on every Actual DB we open (all
// report user_version 0) and creates any table missing from the file with the full current schema,
// so a column like tags.hidden on a pre-tags DB already exists before migrations run. Catching the
// failure is unreliable: on the androidx host-test target it arrives as a message-less
// android.database.SQLException. So we pre-check the column via PRAGMA and skip the ALTER if
// present.
private suspend fun execute(driver: SqlDriver, statement: String) {
  ADD_COLUMN_REGEX.find(statement)?.let { match ->
    val (table, column) = match.destructured
    if (columnExists(driver, table, column)) {
      logcat.w(TAG) { "$table.$column already present, skipping ADD COLUMN" }
      return
    }
  }
  driver.execute(identifier = null, sql = statement, parameters = 0).await()
}

private val ADD_COLUMN_REGEX =
  Regex("""ALTER\s+TABLE\s+(\w+)\s+ADD\s+COLUMN\s+(\w+)""", RegexOption.IGNORE_CASE)

private suspend fun columnExists(driver: SqlDriver, table: String, column: String): Boolean =
  driver
    .executeQuery(
      identifier = null,
      sql = "PRAGMA table_info($table)",
      parameters = 0,
      mapper = { cursor -> cursor.canFindString(column) },
    )
    .await()

private fun SqlCursor.canFindString(column: String) = QueryResult.AsyncValue {
  var found = false
  // PRAGMA table_info columns: cid, name, type, notnull, dflt_value, pk
  while (next().await()) {
    if (getString(1) == column) found = true
  }
  found
}
