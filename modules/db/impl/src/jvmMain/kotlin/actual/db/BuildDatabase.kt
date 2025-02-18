package actual.db

import actual.db.RoomBudgetDatabase.Companion.build
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlin.coroutines.CoroutineContext

fun buildDatabase(
  file: File,
  context: CoroutineContext,
): RoomBudgetDatabase = Room
  .databaseBuilder<RoomBudgetDatabase>(file.absolutePath)
  .setDriver(BundledSQLiteDriver())
  .build(context)
