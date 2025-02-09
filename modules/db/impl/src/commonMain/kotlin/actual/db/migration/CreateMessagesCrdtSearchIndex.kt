package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1612625548236_optimize.sql
 */
internal class CreateMessagesCrdtSearchIndex : Migration(CreateTransactionViews.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        CREATE INDEX messages_crdt_search ON messages_crdt(dataset, row, column, timestamp);

        ANALYZE;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1612625548
  }
}
