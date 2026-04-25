package aktual.test

import aktual.budget.model.BudgetFiles
import okio.FileSystem
import okio.Path

fun testBudgetFiles(fileSystem: FileSystem, root: Path): BudgetFiles =
  BudgetFiles(fileSystem = fileSystem, directoryPath = root)

fun testBudgetFiles(temporaryFolder: ITemporaryFolder): BudgetFiles =
  BudgetFiles(fileSystem = FileSystem.SYSTEM, directoryPath = temporaryFolder.root)
