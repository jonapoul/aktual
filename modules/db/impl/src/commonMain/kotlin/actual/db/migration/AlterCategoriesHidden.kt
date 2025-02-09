package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1685007876842_add_category_hidden.sql
 */
internal class AlterCategoriesHidden : Migration(DropPayeeRules.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE categories ADD COLUMN hidden BOOLEAN NOT NULL DEFAULT 0;
        ALTER TABLE category_groups ADD COLUMN hidden BOOLEAN NOT NULL DEFAULT 0;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1685007876
  }
}
