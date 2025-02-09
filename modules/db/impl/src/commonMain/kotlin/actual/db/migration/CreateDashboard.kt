package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import java.util.UUID

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1722804019000_create_dashboard_table.js
 */
internal class CreateDashboard : Migration(UpdateCustomReports.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        CREATE TABLE dashboard
          (id TEXT PRIMARY KEY,
           type TEXT,
           width INTEGER,
           height INTEGER,
           x INTEGER,
           y INTEGER,
           meta TEXT,
           tombstone INTEGER DEFAULT 0);

        INSERT INTO dashboard (id, type, width, height, x, y)
          VALUES
            ('${UUID.randomUUID()}', 'net-worth-card', 8, 2, 0, 0),
            ('${UUID.randomUUID()}', 'cash-flow-card', 4, 2, 8, 0),
            ('${UUID.randomUUID()}', 'spending-card', 4, 2, 0, 2);
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1722804019
  }
}
