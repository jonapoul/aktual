package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1548957970627_remove-db-version.sql
 */
internal class DropDbVersion : Migration(1, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        DROP TABLE db_version;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1548957970
  }
}
