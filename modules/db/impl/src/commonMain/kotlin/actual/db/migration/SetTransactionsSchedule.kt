package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1720310586000_link_transfer_schedules.sql
 */
internal class SetTransactionsSchedule : Migration(AlterCustomGroupsIncludeCurrent.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        UPDATE transactions AS t1
        SET schedule = (
            SELECT t2.schedule FROM transactions AS t2
            WHERE t2.id = t1.transferred_id
              AND t2.schedule IS NOT NULL
            LIMIT 1
        )
        WHERE t1.schedule IS NULL
        AND t1.transferred_id IS NOT NULL
        AND EXISTS (
          SELECT 1 FROM transactions AS t2
          WHERE t2.id = t1.transferred_id
            AND t2.schedule IS NOT NULL
          LIMIT 1
        );
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1720310586
  }
}
