package actual.core.files

import actual.budget.model.BudgetId
import android.content.Context
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import javax.inject.Inject

class AndroidBudgetFiles @Inject constructor(
  context: Context,
  private val fileSystem: FileSystem,
) : BudgetFiles {
  private val directoryPath = context
    .getDatabasePath("unused")
    .parentFile
    ?.resolve("budgets")
    ?.toOkioPath()
    ?: error("Null budgets directory!")

  override fun directory(id: BudgetId, mkdirs: Boolean): Path = directoryPath
    .resolve(id.value)
    .also { if (mkdirs) fileSystem.createDirectories(it) }
}
