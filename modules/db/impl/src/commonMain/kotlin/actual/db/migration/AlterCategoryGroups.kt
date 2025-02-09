package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1712784523000_unhide_input_group.sql
 */
internal class AlterCategoryGroups : Migration(CreateCustomReports.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        UPDATE category_groups
        SET
          hidden = 0
        WHERE is_income = 1;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1712784523
  }
}
