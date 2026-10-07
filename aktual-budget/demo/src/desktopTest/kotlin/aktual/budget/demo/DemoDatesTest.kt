package aktual.budget.demo

import aktual.budget.BudgetFiles
import aktual.budget.db.AndroidxSqlDriverFactory
import aktual.budget.db.buildDatabase
import aktual.budget.db.migrateDatabase
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath

class DemoDatesTest {
  private lateinit var dir: Path
  private lateinit var driver: SqlDriver
  private lateinit var generatedOn: LocalDate

  // Same driver setup as the app, on a copy of the bundled database
  @BeforeTest
  fun before() = runTest {
    dir = createTempDirectory().toOkioPath()
    val files = BudgetFiles(SYSTEM, dir)
    val bytes = Res.readBytes("files/demo-budget.sqlite")
    FileSystem.SYSTEM.write(files.database(Demo, mkdirs = true)) { write(bytes) }
    driver = AndroidxSqlDriverFactory(files).create(Demo)
    generatedOn = driver.demoGeneratedOn()
  }

  @AfterTest
  fun after() {
    driver.close()
    FileSystem.SYSTEM.deleteRecursively(dir)
  }

  // Reads it through the app's own schema, migrations and column adapters
  @Test
  fun `Loads as an aktual budget`() = runTest {
    driver.shiftDemoDates(
      from = generatedOn,
      to = generatedOn.plus(DatePeriod(days = 5)),
    )
    val db = buildDatabase(driver)
    migrateDatabase(driver, db)

    val ids = db.transactionsQueries.getIds().awaitAsList()
    assertThat(ids).isNotEmpty()
    ids.forEach { id ->
      assertThat(db.transactionsQueries.getByIds(listOf(id)).awaitAsOneOrNull()).isNotNull()
    }
    assertThat(db.schedulesQueries.getFromVSchedules().awaitAsList()).isNotEmpty()
  }

  @Test
  fun `Nothing changes when opened on the generation date`() = runTest {
    val before = queryStrings("SELECT date FROM transactions ORDER BY id")
    driver.shiftDemoDates(from = generatedOn, to = generatedOn)
    assertThat(queryStrings("SELECT date FROM transactions ORDER BY id")).isEqualTo(before)
  }

  @Test
  fun `Shift transactions and schedules by days`() = runTest {
    val period = DatePeriod(days = 40)
    val datesBefore = queryDates("SELECT date FROM transactions ORDER BY id")
    val nextDatesBefore = queryDates("SELECT local_next_date FROM schedules_next_date ORDER BY id")
    val timestampsBefore =
      queryLongs("SELECT base_next_date_ts FROM schedules_next_date ORDER BY id")

    driver.shiftDemoDates(from = generatedOn, to = generatedOn.plus(period))

    assertThat(queryDates("SELECT date FROM transactions ORDER BY id"))
      .isEqualTo(datesBefore.map { it.plus(period) })
    assertThat(queryDates("SELECT local_next_date FROM schedules_next_date ORDER BY id"))
      .isEqualTo(nextDatesBefore.map { it.plus(period) })
    assertThat(queryLongs("SELECT base_next_date_ts FROM schedules_next_date ORDER BY id"))
      .isEqualTo(timestampsBefore.map { it + 40 * 86_400_000L })
  }

  @Test
  fun `Shift budget months by calendar months`() = runTest {
    val monthsBefore = queryStrings("SELECT id FROM zero_budget_months")
    val budgetsBefore = queryLong("SELECT count(*) FROM zero_budgets")

    // Four calendar months later, even though it's fewer than four months' worth of days
    driver.shiftDemoDates(from = LocalDate(2026, 9, 29), to = LocalDate(2027, 1, 1))

    assertThat(queryStrings("SELECT id FROM zero_budget_months").sorted())
      .isEqualTo(monthsBefore.map { it.plusMonths(4) }.sorted())
    assertThat(queryLong("SELECT count(*) FROM zero_budgets")).isEqualTo(budgetsBefore)
    assertThat(queryLong("SELECT count(*) FROM zero_budgets WHERE substr(id, 1, 6) != month"))
      .isEqualTo(0L)
  }

  @Test
  fun `Shift schedule rule dates`() = runTest {
    val period = DatePeriod(days = 3)
    val before = ruleDates()

    driver.shiftDemoDates(from = generatedOn, to = generatedOn.plus(period))

    assertThat(before).isNotEmpty()
    assertThat(ruleDates()).isEqualTo(before.map { it.plus(period) })
  }

  @Test
  fun `Shift one-off and recurring dates in conditions`() {
    fun conditions(oneOff: String, start: String, end: String) =
      """[{"op":"is","field":"date","value":"$oneOff"},""" +
        """{"op":"is","field":"date","value":{"start":"$start","endDate":"$end"}},""" +
        """{"op":"is","field":"notes","value":"2026-01-01"}]"""

    val before = conditions(oneOff = "2026-01-31", start = "2026-01-01", end = "2026-12-31")
    val after = conditions(oneOff = "2026-02-01", start = "2026-01-02", end = "2027-01-01")
    assertThat(shiftConditions(before, DatePeriod(days = 1))).isEqualTo(after)
  }

  private suspend fun queryLong(sql: String): Long = queryLongs(sql).single()

  private suspend fun queryLongs(sql: String): List<Long> = queryStrings(sql).map { it.toLong() }

  private suspend fun queryDates(sql: String): List<LocalDate> =
    queryStrings(sql).map { LocalDate.parse(it, LocalDate.Formats.ISO_BASIC) }

  private suspend fun ruleDates(): List<LocalDate> =
    queryStrings("SELECT conditions FROM rules ORDER BY id").flatMap { conditions ->
      DATE_REGEX.findAll(conditions).map { LocalDate.parse(it.value) }
    }

  // "YYYY-MM"
  private fun String.plusMonths(months: Int): String {
    val date = LocalDate.parse("$this-01").plus(DatePeriod(months = months))
    return date.toString().take(7)
  }

  private suspend fun queryStrings(sql: String): List<String> =
    driver
      .executeQuery(
        identifier = null,
        sql = sql,
        parameters = 0,
        mapper = { cursor ->
          QueryResult.AsyncValue {
            buildList { while (cursor.next().await()) add(requireNotNull(cursor.getString(0))) }
          }
        },
      )
      .await()

  private companion object {
    val DATE_REGEX = Regex("""\d{4}-\d{2}-\d{2}""")
  }
}
