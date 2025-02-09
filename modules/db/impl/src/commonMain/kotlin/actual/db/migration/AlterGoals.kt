package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1694438752000_add_goal_targets.sql
 */
internal class AlterGoals : Migration(AlterSchedulesNextDateTombstone.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE zero_budgets ADD column goal INTEGER DEFAULT null;
        ALTER TABLE reflect_budgets ADD column goal INTEGER DEFAULT null;
        ALTER TABLE categories ADD column goal_def TEXT DEFAULT null;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1694438752
  }
}
