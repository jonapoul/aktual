package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1730744182000_fix_dashboard_table.sql
 */
internal class FixDashboard : Migration(CreatePreferences.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        UPDATE dashboard
        SET tombstone = 1
        WHERE type is NULL;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1730744182
  }
}
