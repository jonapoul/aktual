package aktual.budget.db

import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.utils.BudgetFiles
import aktual.core.utils.UuidGenerator
import aktual.test.CoTemporaryFolder
import aktual.test.doesNotExistOn
import aktual.test.testBudgetFiles
import alakazam.test.TestCoroutineContexts
import app.cash.burst.InterceptTest
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonPrimitive

class BudgetCreatorTest {
  @InterceptTest val temporaryFolder = CoTemporaryFolder()

  private lateinit var files: BudgetFiles
  private var nextId = 0

  @BeforeTest
  fun before() {
    files = testBudgetFiles(temporaryFolder)
  }

  @Test
  fun `Write metadata without a cloud file ID`() = runTest {
    val metadata = creator()(BUDGET_ID, "My Finances")

    val expected = DbMetadata(id = BUDGET_ID.value, budgetName = "My Finances")
    assertThat(metadata).isEqualTo(expected)
    assertThat(files.readMetadata(BUDGET_ID)).isEqualTo(expected)
  }

  @Test
  fun `Seed the database like upstream's new budgets`() = runTest {
    creator()(BUDGET_ID, "My Finances")

    openDatabase { driver, db ->
      val groups = db.categoryGroupsQueries.getAllNames().awaitAsList().map { it.name }
      assertThat(groups).containsExactly("Usual Expenses", "Income", "Investments and Savings")
      assertThat(driver.count("categories")).isEqualTo(7L)
      assertThat(driver.count("category_mapping")).isEqualTo(7L)

      val pages = db.dashboardPagesQueries.getAll().awaitAsList()
      assertThat(pages.map { it.name }).containsExactly("Main")
      assertThat(db.dashboardQueries.getByPage(pages.single().id).awaitAsList()).hasSize(12)

      val migrations = db.migrationsQueries.getAll().awaitAsList()
      assertThat(migrations).isEqualTo(PreAktualMigrations + DatabaseMigrations.map { it.first })
    }
  }

  @Test
  fun `Give the clock a random node`() = runTest {
    // The clock takes the first UUID
    val uuids = UuidGenerator {
      if (nextId++ == 0) "01234567-89ab-cdef-0123-456789abcdef" else "id-$nextId"
    }
    creator(uuids)(BUDGET_ID, "My Finances")

    openDatabase { _, db ->
      val clock = db.messagesClockQueries.getFirst().awaitAsOneOrNull()?.clock
      val timestamp = clock?.get("timestamp")?.jsonPrimitive?.content
      assertThat(timestamp).isEqualTo("1970-01-01T00:00:00.000Z-0000-0123456789abcdef")
    }
  }

  @Test
  fun `Delete the directory on failure`() = runTest {
    val failing = SqlDriverFactory { error("Broken") }
    val creator =
      BudgetCreatorImpl(
        driverFactory = failing,
        files = files,
        uuids = UuidGenerator { "id-${nextId++}" },
        contexts = TestCoroutineContexts(EmptyCoroutineContext),
      )
    files.directory(BUDGET_ID, mkdirs = true)

    val metadata = creator(BUDGET_ID, "My Finances")

    assertThat(metadata).isNull()
    assertThat(files.directory(BUDGET_ID)).doesNotExistOn(files.fileSystem)
  }

  private fun creator(uuids: UuidGenerator = UuidGenerator { "id-${nextId++}" }) =
    BudgetCreatorImpl(
      driverFactory = AndroidxSqlDriverFactory(files),
      files = files,
      uuids = uuids,
      contexts = TestCoroutineContexts(EmptyCoroutineContext),
    )

  private suspend fun openDatabase(block: suspend (SqlDriver, BudgetDatabase) -> Unit) =
    AndroidxSqlDriverFactory(files).create(BUDGET_ID).use { driver ->
      block(driver, buildDatabase(driver))
    }

  private suspend fun SqlDriver.count(table: String): Long =
    executeQuery(
        identifier = null,
        sql = "SELECT COUNT(*) FROM $table",
        mapper = { cursor ->
          cursor.next()
          QueryResult.Value(cursor.getLong(0)!!)
        },
        parameters = 0,
      )
      .await()

  private companion object {
    val BUDGET_ID = BudgetId("new-budget")
  }
}
