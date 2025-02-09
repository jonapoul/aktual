package actual.db

import actual.db.ActualDatabase.Companion.build
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlin.coroutines.CoroutineContext

fun buildDatabase(
  file: File,
  context: CoroutineContext,
): ActualDatabase = Room
  .databaseBuilder<ActualDatabase>(file.absolutePath)
  .setDriver(BundledSQLiteDriver())
  .build(context)
