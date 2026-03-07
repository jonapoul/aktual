package aktual.core.di

import aktual.budget.model.BudgetFiles
import aktual.core.model.AppDirectory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import okio.FileSystem

@BindingContainer
@ContributesTo(AppScope::class)
object BudgetFilesContainer {
  @Provides
  @SingleIn(AppScope::class)
  fun budgetFiles(appDirectory: AppDirectory, fileSystem: FileSystem): BudgetFiles =
    BudgetFiles(fileSystem = fileSystem, directoryPath = appDirectory.get() / "budgets")
}
