package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1691233396000_add_schedule_next_date_tombstone.sql
 */
internal class AlterSchedulesNextDateTombstone : Migration(AlterAccountsType2.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE schedules_next_date ADD COLUMN tombstone INTEGER DEFAULT 0;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1691233396
  }
}
