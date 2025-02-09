package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1681115033845_add_schedule_name.sql
 */
internal class AlterSchedulesName : Migration(AlterRulesConditions.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE schedules ADD COLUMN name TEXT DEFAULT NULL;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1681115033
  }
}
