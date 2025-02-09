package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1736640000000__custom_report_sorting.sql
 */
internal class AlterCustomReportsSortBy : Migration(FixDashboard.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE custom_reports ADD COLUMN sort_by TEXT DEFAULT 'Descending';
        UPDATE custom_reports SET sort_by = 'Descending';
        UPDATE custom_reports SET sort_by = 'Budget' where graph_type = 'TableGraph';
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1736640000
  }
}
