package aktual.test

import aktual.budget.model.BudgetFiles
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import okio.FileSystem
import okio.Path

@BindingContainer
class TestContainer(private val directoryPath: Path) {
  @Provides
  fun budgetFiles(fileSystem: FileSystem): BudgetFiles = BudgetFiles(fileSystem, directoryPath)
}
