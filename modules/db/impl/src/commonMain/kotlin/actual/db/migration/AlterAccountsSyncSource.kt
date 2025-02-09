package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1704572023730_add_account_sync_source.sql
 */
internal class AlterAccountsSyncSource : Migration(AlterTransactionsReconciled.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE accounts ADD COLUMN account_sync_source TEXT;

        UPDATE accounts
        SET
          account_sync_source = 'goCardless'
        WHERE account_id IS NOT NULL
          AND account_sync_source IS NULL;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1704572023
  }
}
