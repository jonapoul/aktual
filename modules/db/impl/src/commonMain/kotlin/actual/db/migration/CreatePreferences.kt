package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1723665565000_prefs.js
 */
internal class CreatePreferences : Migration(CreateDashboard.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        CREATE TABLE preferences
           (id TEXT PRIMARY KEY,
            value TEXT);
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1723665565
  }
}
