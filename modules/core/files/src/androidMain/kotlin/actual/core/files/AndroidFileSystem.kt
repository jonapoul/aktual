package actual.core.files

import actual.budget.model.BudgetId
import android.content.Context
import java.io.File
import javax.inject.Inject

class AndroidFileSystem @Inject constructor(context: Context) : FileSystem {
  private val databaseDirectory = context
    .getDatabasePath("unused")
    .parentFile
    ?: error("Null database directory!")

  override fun databaseDirectory(): File = databaseDirectory

  override fun budgetDatabase(id: BudgetId): File = databaseDirectory.resolve("$id.sqlite")
}
