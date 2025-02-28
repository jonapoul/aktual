package actual.budget.sync.vm

import actual.budget.model.BudgetId
import actual.core.files.FileSystem
import org.junit.rules.TemporaryFolder
import java.io.File

internal class TestFileSystem(temporaryFolder: TemporaryFolder) : FileSystem {
  private val directory = temporaryFolder.newFolder("databases")

  override fun databaseDirectory(): File = directory

  override fun budgetDatabase(id: BudgetId): File = directory.resolve("$id.sqlite")
}
