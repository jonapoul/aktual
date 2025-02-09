package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1688841238000_add_account_type.sql
 */
internal class AlterAccountsType2 : Migration(CreateTransactionFilters.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE accounts ADD COLUMN type TEXT;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1688841238
  }
}
