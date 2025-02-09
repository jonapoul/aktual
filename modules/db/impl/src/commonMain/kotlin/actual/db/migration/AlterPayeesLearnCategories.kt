package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1737158400000_add_learn_categories_to_payees.sql
 */
internal class AlterPayeesLearnCategories : Migration(AlterCustomReportsSortBy.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE payees ADD COLUMN learn_categories BOOLEAN DEFAULT 1;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1737158400
  }
}
