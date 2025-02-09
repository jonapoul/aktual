package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1618975177358_schedules.sql
 */
internal class CreateSchedules : Migration(AlterAccountsSortOrder.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        CREATE TABLE schedules
          (id TEXT PRIMARY KEY,
           rule TEXT,
           active INTEGER DEFAULT 0,
           completed INTEGER DEFAULT 0,
           posts_transaction INTEGER DEFAULT 0,
           tombstone INTEGER DEFAULT 0);

        CREATE TABLE schedules_next_date
          (id TEXT PRIMARY KEY,
           schedule_id TEXT,
           local_next_date INTEGER,
           local_next_date_ts INTEGER,
           base_next_date INTEGER,
           base_next_date_ts INTEGER);

        CREATE TABLE schedules_json_paths
          (schedule_id TEXT PRIMARY KEY,
           payee TEXT,
           account TEXT,
           amount TEXT,
           date TEXT);

        ALTER TABLE transactions ADD COLUMN schedule TEXT;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1618975177
  }
}
