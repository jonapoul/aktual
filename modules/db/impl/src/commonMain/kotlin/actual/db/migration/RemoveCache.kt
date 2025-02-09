package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1632571489012_remove_cache.js
 */
internal class RemoveCache : Migration(CreateSchedules.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        CREATE TABLE zero_budget_months
          (id TEXT PRIMARY KEY,
           buffered INTEGER DEFAULT 0);

        CREATE TABLE zero_budgets
          (id TEXT PRIMARY KEY,
           month INTEGER,
           category TEXT,
           amount INTEGER DEFAULT 0,
           carryover INTEGER DEFAULT 0);

        CREATE TABLE reflect_budgets
          (id TEXT PRIMARY KEY,
           month INTEGER,
           category TEXT,
           amount INTEGER DEFAULT 0,
           carryover INTEGER DEFAULT 0);

        CREATE TABLE notes
          (id TEXT PRIMARY KEY,
           note TEXT);

        CREATE TABLE kvcache (key TEXT PRIMARY KEY, value TEXT);
        CREATE TABLE kvcache_key (id INTEGER PRIMARY KEY, key REAL);

        DROP TABLE spreadsheet_cells;
        ANALYZE;
        VACUUM;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1632571489
  }
}
