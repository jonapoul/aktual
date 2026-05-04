package aktual.budget.db

import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.core.model.AppDirectory
import alakazam.test.getResourceAsStream
import app.cash.sqldelight.db.SqlDriver
import assertk.assertThat
import assertk.assertions.exists
import assertk.assertions.isEqualTo
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okio.FileSystem

abstract class LoadExistingDatabaseFromFileTest {
  protected lateinit var fileSystem: FileSystem
  protected lateinit var budgetFiles: BudgetFiles
  protected lateinit var driver: SqlDriver

  protected abstract fun appDirectory(): AppDirectory

  @BeforeTest
  fun before() {
    fileSystem = FileSystem.SYSTEM
    budgetFiles = BudgetFiles(fileSystem, directoryPath = appDirectory().get())
  }

  @AfterTest
  fun after() {
    driver.close()
  }

  @Test
  fun `Opening existing file and reading table data`() = runTest {
    val file = loadDatabaseIntoFile()
    driver = AndroidxSqlDriverFactory(budgetFiles).create(BUDGET_ID)
    val db = buildDatabase(driver)

    val viewHash =
      db.metaQueries.withResult { getValue(key = "view-hash").executeAsOneOrNull()?.value_ }

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
