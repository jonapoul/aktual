package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1615745967948_meta.sql
 */
internal class DropViews : Migration(CreateTransactionViews2.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        CREATE TABLE __meta__ (key TEXT PRIMARY KEY, value TEXT);

        DROP VIEW IF EXISTS v_transactions_layer2;
        DROP VIEW IF EXISTS v_transactions_layer1;
        DROP VIEW IF EXISTS v_transactions;
        DROP VIEW IF EXISTS v_categories;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1615745967
  }
}
