package aktual.test

import aktual.budget.model.BudgetFiles
import okio.FileSystem
import okio.Path

class TestBudgetFiles(
  override val fileSystem: FileSystem,
  root: Path,
) : BudgetFiles {
  constructor(temporaryFolder: ITemporaryFolder) : this(FileSystem.SYSTEM, temporaryFolder.root)

  override val directoryPath: Path = root
}
