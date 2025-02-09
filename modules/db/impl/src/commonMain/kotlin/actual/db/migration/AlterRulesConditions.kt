package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1679728867040_rules_conditions.sql
 */
internal class AlterRulesConditions : Migration(RemoveCache.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE rules ADD COLUMN conditions_op TEXT DEFAULT 'and';
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1679728867
  }
}
