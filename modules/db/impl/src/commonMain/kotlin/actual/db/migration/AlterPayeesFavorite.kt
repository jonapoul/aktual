package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1720664867241_add_payee_favorite.sql
 */
internal class AlterPayeesFavorite : Migration(SetTransactionsSchedule.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE payees ADD COLUMN favorite INTEGER DEFAULT 0 DEFAULT FALSE;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1720664867
  }
}
