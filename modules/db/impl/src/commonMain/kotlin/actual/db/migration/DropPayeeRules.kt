package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1682974838138_remove_payee_rules.sql
 */
internal class DropPayeeRules : Migration(AlterSchedulesName.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        DROP TABLE payee_rules;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1682974838
  }
}
