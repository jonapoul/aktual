package actual.test

import actual.budget.model.BudgetFiles
import actual.budget.model.BudgetId
import okio.FileSystem
import okio.Path

class TestBudgetFiles(
  override val fileSystem: FileSystem,
  private val root: Path,
) : BudgetFiles {
  constructor(temporaryFolder: ITemporaryFolder) : this(FileSystem.SYSTEM, temporaryFolder.root)

  override fun directory(id: BudgetId, mkdirs: Boolean): Path = root
    .resolve(id.value)
    .also { if (mkdirs) fileSystem.createDirectories(it) }

  override fun tmp(mkdirs: Boolean): Path = root
    .resolve("tmp")
    .also { if (mkdirs) fileSystem.createDirectories(it) }
}
