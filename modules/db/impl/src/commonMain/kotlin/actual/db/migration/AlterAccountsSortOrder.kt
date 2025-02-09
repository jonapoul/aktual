package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1616167010796_accounts_order.sql
 */
internal class AlterAccountsSortOrder : Migration(DropViews.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE accounts ADD COLUMN sort_order REAL;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1616167010
  }
}
