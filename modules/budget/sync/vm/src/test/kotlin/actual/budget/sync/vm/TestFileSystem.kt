package actual.budget.sync.vm

import actual.core.files.FileSystem
import org.junit.rules.TemporaryFolder
import java.io.File

internal class TestFileSystem(private val temporaryFolder: TemporaryFolder) : FileSystem {
  override fun budgetFile(budgetId: String): File = temporaryFolder.newFile(budgetId)
}
