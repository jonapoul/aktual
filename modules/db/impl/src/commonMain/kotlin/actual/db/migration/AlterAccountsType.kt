package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1686139660866_remove_account_type.sql
 */
internal class AlterAccountsType : Migration(AlterCategoriesHidden.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE accounts DROP COLUMN type;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1686139660
  }
}
