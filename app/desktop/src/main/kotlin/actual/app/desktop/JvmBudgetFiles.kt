package actual.app.desktop

import actual.budget.model.BudgetFiles
import actual.core.model.Files
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import okio.FileSystem

@Inject
@ContributesBinding(AppScope::class)
class JvmBudgetFiles(
  files: Files,
  override val fileSystem: FileSystem,
) : BudgetFiles {
  override val directoryPath = files
    .userHome()
    .resolve("budgets")
}
