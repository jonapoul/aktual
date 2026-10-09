package aktual.budget.budgeting.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.DatabaseTables.REFLECT_BUDGETS
import aktual.budget.db.dao.DatabaseTables.ZERO_BUDGETS
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.SyncDao
import aktual.budget.model.Amount
import aktual.budget.model.BudgetId
import aktual.budget.model.CategoryId
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.SyncedPrefKey
import aktual.core.Calendar
import aktual.test.inMemoryDriverFactory
import alakazam.test.TestCoroutineContexts
import app.cash.sqldelight.db.SqlDriver
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

// Ports packages/loot-core/src/server/budget/actions.test.ts
internal class BudgetWriterTest {
  @Test
  fun `New budget sends the month, category and amount`() = runWriterTest {
    writer.setBudget(JAN, CAT1, Amount(5000))

    assertThat(syncCalls.single())
      .containsExactly(
        LocalChange(ZERO_BUDGETS, "202401-cat1", "month", MessageValue.Number(202401)),
        LocalChange(ZERO_BUDGETS, "202401-cat1", "category", MessageValue.String("cat1")),
        LocalChange(ZERO_BUDGETS, "202401-cat1", "amount", MessageValue.Number(5000)),
      )
    assertThat(budgeted(JAN, CAT1)).isEqualTo(Amount(5000))
  }

  @Test
  fun `Existing budget is updated under its own ID`() = runWriterTest {
    run("INSERT INTO zero_budgets(id, month, category, amount) VALUES ('other', 202401, 'cat1', 1)")

    writer.setBudget(JAN, CAT1, Amount(5000))

    assertThat(syncCalls.single())
      .containsExactly(LocalChange(ZERO_BUDGETS, "other", "amount", MessageValue.Number(5000)))
    assertThat(budgeted(JAN, CAT1)).isEqualTo(Amount(5000))
  }

  @Test
  fun `Tracking budgets are written to reflect_budgets`() = runWriterTest {
    tracking()

    writer.setBudget(JAN, CAT1, Amount(5000))

    assertThat(syncCalls.single().map { it.dataset }.toSet()).containsOnly(REFLECT_BUDGETS)
    assertThat(budgeted(JAN, CAT1)).isEqualTo(Amount(5000))
  }

  @Test
  fun `Carryover is set from the month to the end of the bounds in one batch`() = runWriterTest {
    writer.setCarryover(YearMonth(2025, 1), CAT1, enabled = true)

    val changes = syncCalls.single().filter { it.column == "carryover" }
    assertThat(changes.map { it.row }).containsExactly("202501-cat1", "202502-cat1")
    assertThat(changes.map { it.value }.toSet()).containsOnly(MessageValue.Number(1))
  }

  @Test
  fun `Copying the previous month for one category`() = runWriterTest {
    writer.setBudget(JAN, CAT1, Amount(5000))

    writer.copySinglePreviousMonth(FEB, CAT1)

    assertThat(budgeted(FEB, CAT1)).isEqualTo(Amount(5000))
  }

  @Test
  fun `Copying the previous month skips hidden and income categories`() = runWriterTest {
    hideCategory(CAT2)
    writer.setBudget(JAN, CAT1, Amount(5000))
    writer.setBudget(JAN, CAT2, Amount(1000))
    writer.setBudget(JAN, INCOME, Amount(9000))
    syncCalls.clear()

    writer.copyPreviousMonth(FEB)

    assertThat(syncCalls.single().map { it.row }.toSet()).containsOnly("202402-cat1")
    assertThat(budgeted(FEB, CAT1)).isEqualTo(Amount(5000))
  }

  @Test
  fun `Copying the previous month includes income when tracking`() = runWriterTest {
    tracking()
    writer.setBudget(JAN, INCOME, Amount(9000))

    writer.copyPreviousMonth(FEB)

    assertThat(budgeted(FEB, INCOME)).isEqualTo(Amount(9000))
  }

  @Test
  fun `Zeroing a month skips income categories`() = runWriterTest {
    writer.setBudget(JAN, CAT1, Amount(5000))
    writer.setBudget(JAN, INCOME, Amount(9000))
    syncCalls.clear()

    writer.setZero(JAN)

    assertThat(syncCalls.single().map { it.row }.toSet()).containsOnly("202401-cat1", "202401-cat2")
    assertThat(budgeted(JAN, CAT1)).isEqualTo(Zero)
  }

  @Test
  fun `Nothing is sent when there's nothing to copy`() = runWriterTest {
    writer.copyPreviousMonth(FEB)

    assertThat(syncCalls).isEmpty()
  }

  @Test
  fun `copyUntilYearEnd copies to all future months in the same year`() = runWriterTest {
    writer.setBudget(JAN, CAT1, Amount(5000))
    writer.setBudget(FEB, CAT1, Amount(1000))
    writer.setBudget(MAR, CAT1, Amount(2000))

    writer.copyUntilYearEnd(JAN, CAT1)

    assertThat(budgeted(JAN, CAT1)).isEqualTo(Amount(5000))
    assertThat(budgeted(FEB, CAT1)).isEqualTo(Amount(5000))
    assertThat(budgeted(MAR, CAT1)).isEqualTo(Amount(5000))
  }

  @Test
  fun `copyUntilYearEnd overwrites future months including zero budgets`() = runWriterTest {
    writer.setBudget(JAN, CAT1, Amount(5000))
    writer.setBudget(MAR, CAT1, Amount(2000))

    writer.copyUntilYearEnd(JAN, CAT1)

    assertThat(budgeted(FEB, CAT1)).isEqualTo(Amount(5000))
    assertThat(budgeted(MAR, CAT1)).isEqualTo(Amount(5000))
  }

  @Test
  fun `copyUntilYearEnd leaves earlier months alone`() = runWriterTest {
    writer.setBudget(JAN, CAT1, Amount(1000))
    writer.setBudget(FEB, CAT1, Amount(5000))
    writer.setBudget(MAR, CAT1, Amount(2000))

    writer.copyUntilYearEnd(FEB, CAT1)

    assertThat(budgeted(JAN, CAT1)).isEqualTo(Amount(1000))
    assertThat(budgeted(FEB, CAT1)).isEqualTo(Amount(5000))
    assertThat(budgeted(MAR, CAT1)).isEqualTo(Amount(5000))
  }

  @Test
  fun `copyUntilYearEnd works for tracking budgets`() = runWriterTest {
    tracking()
    writer.setBudget(JAN, CAT1, Amount(5000))
    writer.setBudget(FEB, CAT1, Amount(1000))
    writer.setBudget(MAR, CAT1, Amount(2000))

    writer.copyUntilYearEnd(JAN, CAT1)

    assertThat(budgeted(FEB, CAT1)).isEqualTo(Amount(5000))
    assertThat(budgeted(MAR, CAT1)).isEqualTo(Amount(5000))
  }

  @Test
  fun `copyUntilYearEnd stops at December`() = runWriterTest {
    val nov = YearMonth(2024, 11)
    val dec = YearMonth(2024, 12)
    val jan = YearMonth(2025, 1)
    writer.setBudget(nov, CAT1, Amount(5000))
    writer.setBudget(dec, CAT1, Amount(1000))
    writer.setBudget(jan, CAT1, Amount(2000))
    syncCalls.clear()

    writer.copyUntilYearEnd(nov, CAT1)

    assertThat(syncCalls.single().map { it.row }.toSet()).containsOnly("202412-cat1")
    assertThat(budgeted(dec, CAT1)).isEqualTo(Amount(5000))
    assertThat(budgeted(jan, CAT1)).isEqualTo(Amount(2000))
  }

  @Test
  fun `Single category average from complete months`() = runWriterTest {
    insertAverageTransactions()

    writer.setSingleAverage(APR, CAT1, months = 3)

    assertThat(budgeted(APR, CAT1)).isEqualTo(Amount(600))
  }

  @Test
  fun `Single category average from the first activity month`() = runWriterTest {
    insertAverageTransactions()
    writer.setBudget(YearMonth(2023, 12), CAT1, Amount(1000))

    writer.setSingleAverage(APR, CAT1, months = 3)

    assertThat(budgeted(APR, CAT1)).isEqualTo(Amount(600))
  }

  @Test
  fun `Single category average is rounded`() = runWriterTest {
    insertAverageTransactions()
    insertTransaction("t6", 20231220, amount = -100)

    writer.setSingleAverage(APR, CAT1, months = 3)

    assertThat(budgeted(APR, CAT1)).isEqualTo(Amount(633))
  }

  @Test
  fun `Average stops before the first activity month`() = runWriterTest {
    insertAverageTransactions()

    writer.setSingleAverage(APR, CAT1, months = 12)

    // (300 + 600 + 900) / 3, nothing before November 2023
    assertThat(budgeted(APR, CAT1)).isEqualTo(Amount(600))
  }

  @Test
  fun `Bulk 3 month average from complete months`() = runWriterTest {
    insertAverageTransactions()

    writer.setAverage(APR, months = 3)

    assertThat(budgeted(APR, CAT1)).isEqualTo(Amount(600))
  }

  @Test
  fun `Bulk 3 month average from the first activity month`() = runWriterTest {
    insertAverageTransactions()
    writer.setBudget(YearMonth(2023, 12), CAT1, Amount(1000))

    writer.setAverage(APR, months = 3)

    assertThat(budgeted(APR, CAT1)).isEqualTo(Amount(600))
  }

  @Test
  fun `Bulk average skips hidden and income categories`() = runWriterTest {
    insertAverageTransactions()
    hideCategory(CAT2)

    writer.setAverage(APR, months = 3)

    assertThat(syncCalls.single().map { it.row }.toSet()).containsOnly("202404-cat1")
  }

  private class WriterTestScope(
    database: BudgetDatabase,
    private val driver: SqlDriver,
    private val syncDao: SyncDao,
  ) : BudgetSyncController {
    val syncCalls = mutableListOf<List<LocalChange>>()
    private val contexts = TestCoroutineContexts(EmptyCoroutineContext)
    private val preferences = PreferencesDao(database, contexts)
    private val budgetDao = BudgetDao(database, contexts)
    private val calendar = Calendar { TODAY }

    val writer = BudgetWriterImpl(this, budgetDao, preferences, calendar)
    val calculator = BudgetMonthCalculatorImpl(budgetDao, preferences, calendar, contexts)

    override suspend fun syncChanges(changes: List<LocalChange>) {
      syncCalls.add(changes)
      syncDao.sendMessages(changes)
    }

    override fun schedule() = Unit

    suspend fun budgeted(month: YearMonth, category: CategoryId): Amount =
      calculator.observe(month).first().categories.single { it.id == category }.budgeted

    suspend fun tracking() {
      preferences[SyncedPrefKey.Global.BudgetType] = "tracking"
    }

    suspend fun hideCategory(id: CategoryId) =
      run("UPDATE categories SET hidden = 1 WHERE id = '${id.value}'")

    suspend fun insertTransaction(id: String, date: Int, amount: Int) =
      run(
        "INSERT INTO transactions(id, acct, category, amount, date, tombstone) " +
          "VALUES ('$id', 'account1', 'cat1', $amount, $date, 0)",
      )

    // setupAverageDatabase()
    suspend fun insertAverageTransactions() {
      insertTransaction("t1", 20231115, amount = -300)
      insertTransaction("t2", 20231215, amount = -600)
      insertTransaction("t3", 20240115, amount = -900)
      insertTransaction("t4", 20240215, amount = -3000)
      insertTransaction("t5", 20240315, amount = -1200)
    }

    suspend fun run(sql: String) {
      driver.execute(identifier = null, sql = sql, parameters = 0).await()
    }
  }

  private fun runWriterTest(action: suspend WriterTestScope.() -> Unit) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use {
      val database = buildDatabase(driver)
      val scope = WriterTestScope(database, driver, SyncDao(database, driver, Clock.System))
      SETUP.forEach { sql -> scope.run(sql) }
      scope.action()
    }
  }

  private companion object {
    // global.currentMonth = '2024-02', so the budget runs to February 2025
    val TODAY = LocalDate(2024, 2, 15)

    val JAN = YearMonth(2024, 1)
    val FEB = YearMonth(2024, 2)
    val MAR = YearMonth(2024, 3)
    val APR = YearMonth(2024, 4)

    val CAT1 = CategoryId("cat1")
    val CAT2 = CategoryId("cat2")
    val INCOME = CategoryId("income-cat")

    @Suppress("MaxLineLength")
    val SETUP =
      listOf(
        "INSERT INTO category_groups(id, name, is_income, sort_order) VALUES ('income-group', 'Income', 1, 2)",
        "INSERT INTO category_groups(id, name, is_income, sort_order) VALUES ('group1', 'group1', 0, 1)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('income-cat', 'Income', 1, 'income-group', 1)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('cat1', 'cat1', 0, 'group1', 1)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('cat2', 'cat2', 0, 'group1', 2)",
        "INSERT INTO category_mapping(id, transferId) VALUES ('income-cat', 'income-cat'), ('cat1', 'cat1'), ('cat2', 'cat2')",
        "INSERT INTO accounts(id, name, offbudget) VALUES ('account1', 'Account 1', 0)",
      )
  }
}
