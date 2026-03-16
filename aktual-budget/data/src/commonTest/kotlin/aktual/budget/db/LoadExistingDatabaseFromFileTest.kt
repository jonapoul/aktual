package aktual.budget.db

import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.model.database
import aktual.test.runDatabaseTest
import alakazam.test.getResourceAsStream
import assertk.assertThat
import assertk.assertions.exists
import assertk.assertions.isEqualTo
import java.io.File
import kotlin.test.Test
import okio.FileSystem

abstract class LoadExistingDatabaseFromFileTest {
  protected val fileSystem: FileSystem = FileSystem.SYSTEM
  protected lateinit var budgetFiles: BudgetFiles
  protected lateinit var provider: BudgetDatabase.BuilderProvider

  @Test
  fun `Opening existing file and reading table data`() =
    runDatabaseTest(provider) {
      val file = loadDatabaseIntoFile()
      val db = buildDatabase(BUDGET_ID)
      val viewHash = db.meta().getValue(key = "view-hash")
      assertThat(viewHash).isEqualTo("c379fa428efd55a684aba4947ad054e0")
      assertThat(file).exists()
    }

  private fun loadDatabaseIntoFile(): File {
    val databaseFile = budgetFiles.database(BUDGET_ID, mkdirs = true).toFile()
    getResourceAsStream("test-db.sqlite").use { input ->
      databaseFile.outputStream().use { output -> input.copyTo(output) }
    }
    return databaseFile
  }

  private companion object {
    val BUDGET_ID = BudgetId("abc-123")
  }
}
