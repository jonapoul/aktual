package aktual.test

import aktual.budget.model.BudgetFiles
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import okio.FileSystem

@BindingContainer
class TestContainer(private val temporaryFolder: TemporaryFolder) {
  @Provides
  fun budgetFiles(fileSystem: FileSystem): BudgetFiles =
    BudgetFiles(fileSystem, directoryPath = temporaryFolder.root)
}
