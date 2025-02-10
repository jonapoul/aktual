package actual.db

import actual.db.BudgetDatabase.Companion.build
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlin.coroutines.CoroutineContext

fun buildDatabase(
  file: File,
  context: CoroutineContext,
): BudgetDatabase = Room
  .databaseBuilder<BudgetDatabase>(file.absolutePath)
  .setDriver(BundledSQLiteDriver())
  .build(context)
