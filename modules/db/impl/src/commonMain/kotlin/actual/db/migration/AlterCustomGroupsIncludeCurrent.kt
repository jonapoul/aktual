package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1716359441000_include_current.sql
 */
internal class AlterCustomGroupsIncludeCurrent : Migration(AlterCategoryGroups.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE custom_reports ADD COLUMN include_current INTEGER DEFAULT 0;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1716359441
  }
}
