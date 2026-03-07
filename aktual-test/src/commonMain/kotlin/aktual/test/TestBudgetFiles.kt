package aktual.test

import aktual.budget.model.BudgetFiles
import okio.FileSystem
import okio.Path

class TestBudgetFiles(fileSystem: FileSystem, root: Path) : BudgetFiles(fileSystem, root) {
  constructor(temporaryFolder: ITemporaryFolder) : this(FileSystem.SYSTEM, temporaryFolder.root)
}
