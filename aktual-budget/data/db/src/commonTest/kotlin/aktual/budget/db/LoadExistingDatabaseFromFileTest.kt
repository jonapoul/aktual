package aktual.budget.db

import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.model.CleanupGroupId
import aktual.budget.model.CustomReportId
import aktual.budget.model.ScheduleId
import aktual.test.CoTemporaryFolder
import aktual.test.testBudgetFiles
import alakazam.test.getResourceAsStream
import app.cash.burst.InterceptTest
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
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

class LoadExistingDatabaseFromFileTest {
  @InterceptTest val temporaryFolder = CoTemporaryFolder()

  private lateinit var budgetFiles: BudgetFiles
  private lateinit var driver: SqlDriver

  @BeforeTest
  fun before() {
    budgetFiles = testBudgetFiles(temporaryFolder)
  }

  @AfterTest
  fun after() {
    driver.close()
  }

  @Test
  fun `Fresh database has all migrations seeded by onCreate`() = runTest {
    driver = AndroidxSqlDriverFactory(budgetFiles).create(BUDGET_ID)
    val db = buildDatabase(driver)
    migrateDatabase(driver, db)

    val recorded = db.migrationsQueries.getAll().awaitAsList()
    assertThat(recorded).isEqualTo(DatabaseMigrations.map { it.first })
  }

  @Test
  fun `Opening existing file and validate migrations`() = runTest {
    val file = loadDatabaseIntoFile()
    driver = AndroidxSqlDriverFactory(budgetFiles).create(BUDGET_ID)

    // DB starts at migration number 1738491452000
    val db = buildDatabase(driver)
    migrateDatabase(driver, db)

    assertThat(file).exists()

    assertViewHash(db)
    checkMigration1769000000000(db)
    checkMigration1778510362740(db)
    checkMigration1780099200000(db)
  }

  // Verify that the file was opened at all
  private suspend fun assertViewHash(db: BudgetDatabase) {
    val viewHash = db.metaQueries.getValue(key = "view-hash").awaitAsOneOrNull()?.value_
    assertThat(viewHash).isEqualTo("c379fa428efd55a684aba4947ad054e0")
  }

  // Adds custom_upcoming_length column. migration sets to null by default
  private suspend fun checkMigration1769000000000(db: BudgetDatabase) {
    val customUpcomingLength =
      db.schedulesQueries.getCustomUpcomingLength(id = ScheduleId("schedule-id")).awaitAsOneOrNull()
    assertNull(customUpcomingLength)
  }

  // Adds cleanup_def column and cleanup_group table. migration sets to null by default
  private suspend fun checkMigration1778510362740(db: BudgetDatabase) {
    val cleanupGroup =
      db.cleanupGroupsQueries.getById(CleanupGroupId("cleanup-group")).awaitAsOneOrNull()
    assertNull(cleanupGroup)
  }

  // Adds show_trend_lines column to custom_reports. migration sets to 0 (false) by default
  private suspend fun checkMigration1780099200000(db: BudgetDatabase) {
    val showTrendLines =
      db.customReportsQueries
        .getShowTrendLines(id = CustomReportId("custom-report"))
        .awaitAsOneOrNull()
    assertNull(showTrendLines)
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
