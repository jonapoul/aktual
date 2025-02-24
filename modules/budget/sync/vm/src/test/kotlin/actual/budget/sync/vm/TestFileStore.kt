package actual.budget.sync.vm

import actual.api.download.FileStore
import actual.budget.model.BudgetId
import org.junit.rules.TemporaryFolder
import java.io.File

internal class TestFileStore(private val temporaryFolder: TemporaryFolder) : FileStore {
  override fun budgetFile(id: BudgetId): File = temporaryFolder.newFile(id.filename())
}
