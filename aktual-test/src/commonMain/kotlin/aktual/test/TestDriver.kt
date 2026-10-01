package aktual.test

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.SqlDriverFactory
import aktual.budget.db.buildDatabase
import aktual.budget.model.BudgetId
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import java.util.Properties
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

fun inMemoryDriverFactory(): SqlDriverFactory =
  JdbcSqlDriverFactory(url = JdbcSqliteDriver.IN_MEMORY)

fun fileDriverFactory(file: File): SqlDriverFactory = JdbcSqlDriverFactory(file)

fun runDatabaseTest(
  id: BudgetId = BudgetId("abc-123"),
  driverFactory: SqlDriverFactory = inMemoryDriverFactory(),
  action: suspend BudgetDatabase.(TestScope) -> Unit,
) = runTest {
  val driver = driverFactory.create(id)
  val database = buildDatabase(driver)
  driver.use { action(database, this) }
}

private class JdbcSqlDriverFactory(private val url: String) : SqlDriverFactory {
  constructor(file: File) : this(url = "jdbc:sqlite:${file.absolutePath}")

  override fun create(budgetId: BudgetId): SqlDriver {
    // A Robolectric test registers the driver from its sandbox classloader, which DriverManager then
    // hides from plain JVM tests in the same process. Loading it here registers it for our classloader
    Class.forName("org.sqlite.JDBC")
    return JdbcSqliteDriver(
      url = url,
      schema = BudgetDatabase.Schema.synchronous(),
      properties = Properties().apply { put("foreign_keys", "true") },
    )
  }
}
