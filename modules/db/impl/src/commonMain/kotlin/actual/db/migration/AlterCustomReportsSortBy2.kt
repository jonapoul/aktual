package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1738491452000__sorting_rename.sql
 */
internal class AlterCustomReportsSortBy2 : Migration(AlterPayeesLearnCategories.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE custom_reports RENAME COLUMN sort_by TO sort_by_old;
        ALTER TABLE custom_reports ADD COLUMN sort_by TEXT DEFAULT 'desc';

        UPDATE custom_reports SET sort_by = 'desc' where sort_by_old = 'Descending';
        UPDATE custom_reports SET sort_by = 'asc' where sort_by_old = 'Ascending';
        UPDATE custom_reports SET sort_by = 'budget' where sort_by_old = 'Budget';
        UPDATE custom_reports SET sort_by = 'name' where sort_by_old = 'Name';

        ALTER TABLE custom_reports DROP COLUMN sort_by_old;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1738491452
  }
}
