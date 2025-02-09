package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1597756566448_rules.sql
 */
internal class CreateRules : Migration(AlterTransactionsClearedPending.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        CREATE TABLE rules
          (id TEXT PRIMARY KEY,
           stage TEXT,
           conditions TEXT,
           actions TEXT,
           tombstone INTEGER DEFAULT 0);
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1597756566
  }
}
