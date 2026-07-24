package aktual.di

import aktual.budget.BudgetFiles
import aktual.core.AppDirectory
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
