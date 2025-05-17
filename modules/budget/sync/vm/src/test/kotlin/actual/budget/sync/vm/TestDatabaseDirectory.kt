package actual.budget.sync.vm

import actual.budget.model.BudgetId
import actual.core.files.DatabaseDirectory
import okio.Path
import okio.fakefilesystem.FakeFileSystem

internal class TestDatabaseDirectory(private val fileSystem: FakeFileSystem) : DatabaseDirectory {
  override fun pathFor(id: BudgetId): Path = fileSystem.workingDirectory.resolve("$id.sqlite")
}
