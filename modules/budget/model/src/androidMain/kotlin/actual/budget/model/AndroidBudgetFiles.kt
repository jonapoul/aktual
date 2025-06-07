package actual.budget.model

import android.content.Context
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import javax.inject.Inject

class AndroidBudgetFiles @Inject constructor(
  context: Context,
  override val fileSystem: FileSystem,
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

  override fun tmp(mkdirs: Boolean): Path = directoryPath
    .resolve("tmp")
    .also { if (mkdirs) fileSystem.createDirectories(it) }
}
