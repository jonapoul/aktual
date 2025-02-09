package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1722717601000_reports_move_selected_categories.js
 *
 * Significantly simplified from the actual SQL file linked above
 */
internal class UpdateCustomReports : Migration(AlterLongGoal.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        UPDATE custom_reports SET selected_categories = NULL WHERE tombstone = 0
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1722717601
  }
}
