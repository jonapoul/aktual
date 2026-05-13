package aktual.budget.db

import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.model.CleanupGroupId
import aktual.budget.model.ScheduleId
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
import kotlin.test.assertNull
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
  fun `Opening existing file and validate migrations`() = runTest {
    val file = loadDatabaseIntoFile()
    driver = AndroidxSqlDriverFactory(budgetFiles).create(BUDGET_ID)

    // DB starts at migration number 1738491452000
    val db = buildDatabase(driver)
    migrateDatabase(driver, db).await()

    assertThat(file).exists()

    assertViewHash(db)
    checkMigration1769000000000(db)
    checkMigration1778510362740(db)
  }

  // Verify that the file was opened at all
  private fun assertViewHash(db: BudgetDatabase) {
    val viewHash = db.metaQueries.getValue(key = "view-hash").executeAsOneOrNull()?.value_
    assertThat(viewHash).isEqualTo("c379fa428efd55a684aba4947ad054e0")
  }

  // Adds custom_upcoming_length column. migration sets to null by default
  private fun checkMigration1769000000000(db: BudgetDatabase) {
    val customUpcomingLength =
      db.schedulesQueries.getCustomUpcomingLength(id = ScheduleId("schedule-id"))
    assertNull(customUpcomingLength.executeAsOneOrNull())
  }

  // Adds cleanup_def column and cleanup_group table. migration sets to null by default
  private fun checkMigration1778510362740(db: BudgetDatabase) {
    val cleanupGroup = db.cleanupGroupsQueries.getById(CleanupGroupId("cleanup-group"))
    assertNull(cleanupGroup.executeAsOneOrNull())
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
