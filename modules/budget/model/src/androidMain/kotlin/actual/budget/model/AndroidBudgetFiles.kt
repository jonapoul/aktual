package actual.budget.model

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath

@Inject
@ContributesBinding(scope = AppScope::class, binding = binding<BudgetFiles>())
class AndroidBudgetFiles(
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
