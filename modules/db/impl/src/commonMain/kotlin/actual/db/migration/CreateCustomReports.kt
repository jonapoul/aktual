package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1707267033000_reports.sql
 */
internal class CreateCustomReports : Migration(AlterAccountsSyncSource.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        CREATE TABLE custom_reports
          (
            id TEXT PRIMARY KEY,
            name TEXT,
            start_date TEXT,
            end_date TEXT,
            date_static INTEGER DEFAULT 0,
            date_range TEXT,
            mode TEXT DEFAULT 'total',
            group_by TEXT DEFAULT 'Category',
            balance_type TEXT DEFAULT 'Expense',
            show_empty INTEGER DEFAULT 0,
            show_offbudget INTEGER DEFAULT 0,
            show_hidden INTEGER DEFAULT 0,
            show_uncategorized INTEGER DEFAULT 0,
            selected_categories TEXT,
            graph_type TEXT DEFAULT 'BarGraph',
            conditions TEXT,
            conditions_op TEXT DEFAULT 'and',
            metadata TEXT,
            interval TEXT DEFAULT 'Monthly',
            color_scheme TEXT,
            tombstone INTEGER DEFAULT 0
          );
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1707267033
  }
}
