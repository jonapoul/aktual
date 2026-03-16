package aktual.budget.db

import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import logcat.logcat

internal class BudgetDatabaseCallback : RoomDatabase.Callback() {
  override suspend fun onCreate(connection: SQLiteConnection) {
    logcat.d { "onCreate $connection" }
    super.onCreate(connection)
  }

  override suspend fun onDestructiveMigration(connection: SQLiteConnection) {
    logcat.d { "onDestructiveMigration $connection" }
    super.onDestructiveMigration(connection)
  }

  override suspend fun onOpen(connection: SQLiteConnection) {
    logcat.d { "onOpen $connection" }
    super.onOpen(connection)
  }
}
