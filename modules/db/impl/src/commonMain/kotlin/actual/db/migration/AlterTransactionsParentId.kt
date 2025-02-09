package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1608652596043_parent_field.sql
 */
internal class AlterTransactionsParentId : Migration(CreateRules.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE transactions ADD COLUMN parent_id TEXT;

        UPDATE transactions SET
          parent_id = CASE
            WHEN isChild THEN SUBSTR(id, 1, INSTR(id, '/') - 1)
            ELSE NULL
          END;

        CREATE INDEX trans_parent_id ON transactions(parent_id);
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1608652596
  }
}
