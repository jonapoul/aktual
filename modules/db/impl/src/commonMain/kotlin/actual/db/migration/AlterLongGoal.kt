package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1720665000000_goal_context.sql
 */
internal class AlterLongGoal : Migration(AlterPayeesFavorite.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        ALTER TABLE zero_budgets ADD COLUMN long_goal INTEGER DEFAULT null;
        ALTER TABLE reflect_budgets ADD COLUMN long_goal INTEGER DEFAULT null;
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1720665000
  }
}
