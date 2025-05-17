package actual.core.files

import actual.budget.model.BudgetId
import android.content.Context
import okio.Path
import okio.Path.Companion.toOkioPath
import javax.inject.Inject

class AndroidDatabaseDirectory @Inject constructor(context: Context) : DatabaseDirectory {
  private val directoryPath = context
    .getDatabasePath("unused")
    .parentFile
    ?.toOkioPath()
    ?: error("Null database directory!")

  override fun pathFor(id: BudgetId): Path = directoryPath.resolve("$id.sqlite")
}
