package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1582384163573_cleared.sql
 */
internal class AlterTransactionsClearedPending : Migration(UpdateSpreadsheetCells.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE transactions ADD COLUMN cleared INTEGER DEFAULT 1;
        ALTER TABLE transactions ADD COLUMN pending INTEGER DEFAULT 0;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1582384163
  }
}
