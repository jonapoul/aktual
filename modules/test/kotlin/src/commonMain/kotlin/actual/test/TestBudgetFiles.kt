package actual.test

import actual.budget.model.BudgetFiles
import actual.budget.model.BudgetId
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import org.junit.rules.TemporaryFolder

class TestBudgetFiles(
  override val fileSystem: FileSystem,
  private val root: Path,
) : BudgetFiles {
  constructor(temporaryFolder: TemporaryFolder) : this(FileSystem.SYSTEM, temporaryFolder.root.toOkioPath())

  override fun directory(id: BudgetId, mkdirs: Boolean): Path = root
    .resolve(id.value)
    .also { if (mkdirs) fileSystem.createDirectories(it) }

  override fun tmp(mkdirs: Boolean): Path = root
    .resolve("tmp")
    .also { if (mkdirs) fileSystem.createDirectories(it) }
}
