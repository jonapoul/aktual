package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1555786194328_remove_category_group_unique.sql
 */
internal class UpdateCategoryGroup : Migration(CreatePayees.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        CREATE TEMPORARY TABLE category_groups_tmp
           (id TEXT PRIMARY KEY,
            name TEXT UNIQUE,
            is_income INTEGER DEFAULT 0,
            sort_order REAL,
            tombstone INTEGER DEFAULT 0);

        INSERT INTO category_groups_tmp SELECT * FROM category_groups;

        DROP TABLE category_groups;

        CREATE TABLE category_groups
           (id TEXT PRIMARY KEY,
            name TEXT,
            is_income INTEGER DEFAULT 0,
            sort_order REAL,
            tombstone INTEGER DEFAULT 0);

        INSERT INTO category_groups SELECT * FROM category_groups_tmp;

        DROP TABLE category_groups_tmp;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1555786194
  }
}
