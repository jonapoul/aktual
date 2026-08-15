package aktual.test

import aktual.budget.BudgetFiles
import okio.FileSystem
import okio.Path

fun testBudgetFiles(fileSystem: FileSystem, root: Path): BudgetFiles =
  BudgetFiles(fileSystem = fileSystem, directoryPath = root)

fun testBudgetFiles(temporaryFolder: ITemporaryFolder): BudgetFiles =
  BudgetFiles(fileSystem = SYSTEM, directoryPath = temporaryFolder.root)
