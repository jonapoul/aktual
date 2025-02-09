package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1697046240000_add_reconciled.sql
 */
internal class AlterTransactionsReconciled : Migration(AlterGoals.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE transactions ADD COLUMN reconciled INTEGER DEFAULT 0;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1697046240
  }
}
