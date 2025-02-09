package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1561751833510_indexes.sql
 */
internal class CreateTransactionIndices : Migration(UpdateCategoryGroup.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        CREATE INDEX trans_category_date ON transactions(category, date);
        CREATE INDEX trans_category ON transactions(category);
        CREATE INDEX trans_date ON transactions(date);
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1561751833
  }
}
