package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1688749527273_transaction_filters.sql
 */
internal class CreateTransactionFilters : Migration(AlterAccountsType.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        CREATE TABLE transaction_filters
          (id TEXT PRIMARY KEY,
           name TEXT,
           conditions TEXT,
           conditions_op TEXT DEFAULT 'and',
           tombstone INTEGER DEFAULT 0);
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1688749527
  }
}
