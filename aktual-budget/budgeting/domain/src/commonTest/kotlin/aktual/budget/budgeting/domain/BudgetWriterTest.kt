package aktual.budget.budgeting.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.DatabaseTables.NOTES
import aktual.budget.db.dao.DatabaseTables.REFLECT_BUDGETS
import aktual.budget.db.dao.DatabaseTables.ZERO_BUDGETS
import aktual.budget.db.dao.DatabaseTables.ZERO_BUDGET_MONTHS
import aktual.budget.db.dao.NotesDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.SyncDao
import aktual.budget.model.Amount
import aktual.budget.model.BudgetId
import aktual.budget.model.CategoryId
import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.NumberFormat
import aktual.budget.model.SyncedPrefKey
import aktual.core.Calendar
import aktual.prefs.CurrencyPreferences
import aktual.prefs.FormatPreferences
import aktual.prefs.Preference
import aktual.test.inMemoryDriverFactory
import alakazam.test.TestCoroutineContexts
import app.cash.sqldelight.db.SqlDriver
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
  fun `Tracking average starts from the first tracking budget row`() = runWriterTest {
    insertAverageTransactions()
    tracking()
    writer.setBudget(YearMonth(2023, 10), CAT1, Amount(1000))

    writer.setSingleAverage(APR, CAT1, months = 12)

    // (300 + 600 + 900) / 4, counting October 2023
    assertThat(budgeted(APR, CAT1)).isEqualTo(Amount(450))
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

  @Test
  fun `coverOverbudgeted fully covers when the category has enough left over`() = runWriterTest {
    insertOverbudgeted()

    writer.coverOverbudgeted(FEB, CAT1)

    assertThat(envelope(FEB).toBudget).isEqualTo(Zero)
    assertThat(balance(FEB, CAT1)).isEqualTo(Amount(10))
  }

  @Test
  fun `coverOverbudgeted partially covers when the category is short`() = runWriterTest {
    insertOverbudgeted()

    writer.coverOverbudgeted(FEB, CAT3)

    assertThat(envelope(FEB).toBudget).isEqualTo(Amount(-80))
    assertThat(balance(FEB, CAT3)).isEqualTo(Zero)
  }

  @Test
  fun `Transferring between categories moves the budget and adds a note`() = runWriterTest {
    writer.setBudget(JAN, CAT1, Amount(5000))
    syncCalls.clear()

    writer.transferCategory(JAN, Amount(2000), from = CAT1, to = CAT2)

    assertThat(budgeted(JAN, CAT1)).isEqualTo(Amount(3000))
    assertThat(budgeted(JAN, CAT2)).isEqualTo(Amount(2000))
    assertThat(syncCalls.single().last())
      .isEqualTo(
        LocalChange(
          dataset = NOTES,
          row = "budget-2024-01",
          column = "note",
          value = MessageValue.String("- Reassigned 20.00 from cat1 → cat2 on February 15"),
        ),
      )
  }

  @Test
  fun `Transferring to To Budget only changes the source`() = runWriterTest {
    writer.setBudget(JAN, CAT1, Amount(5000))
    syncCalls.clear()

    writer.transferCategory(JAN, Amount(2000), from = CAT1, to = null)

    assertThat(budgeted(JAN, CAT1)).isEqualTo(Amount(3000))
    assertThat(budgeted(JAN, CAT2)).isEqualTo(Zero)
    assertThat(note(JAN)).isEqualTo("- Reassigned 20.00 from cat1 → To Budget on February 15")
  }

  @Test
  fun `Movement note is appended to an existing note`() = runWriterTest {
    run("INSERT INTO notes(id, note) VALUES ('budget-2024-01', 'Existing')")

    writer.transferCategory(JAN, Amount(2000), from = CAT1, to = CAT2)

    assertThat(note(JAN)).isEqualTo("Existing\n- Reassigned 20.00 from cat1 → cat2 on February 15")
  }

  @Test
  fun `Transferring from To Budget is clamped to what's available`() = runWriterTest {
    insertTransaction("income", 20240110, amount = 10000, category = INCOME)

    writer.transferAvailable(JAN, Amount(15000), CAT1)

    assertThat(budgeted(JAN, CAT1)).isEqualTo(Amount(10000))
    assertThat(envelope(JAN).toBudget).isEqualTo(Zero)
  }

  @Test
  fun `Covering overspending is capped by the source's balance`() = runWriterTest {
    insertOverspending()

    writer.coverOverspending(JAN, to = CAT1, from = CAT2)

    assertThat(budgeted(JAN, CAT1)).isEqualTo(Amount(1500))
    assertThat(budgeted(JAN, CAT2)).isEqualTo(Zero)
    assertThat(note(JAN)).isEqualTo("- Reassigned 5.00 from cat2 → cat1 on February 15")
  }

  @Test
  fun `Covering part of the overspending`() = runWriterTest {
    insertOverspending()

    writer.coverOverspending(JAN, to = CAT1, from = CAT2, amount = Amount(300))

    assertThat(budgeted(JAN, CAT1)).isEqualTo(Amount(1300))
    assertThat(budgeted(JAN, CAT2)).isEqualTo(Amount(200))
  }

  @Test
  fun `Covering overspending from To Budget only changes the target`() = runWriterTest {
    insertOverspending()
    insertTransaction("income", 20240110, amount = 10000, category = INCOME)
    syncCalls.clear()

    writer.coverOverspending(JAN, to = CAT1, from = null)

    assertThat(syncCalls.single().map { it.row }).containsExactly("202401-cat1", "budget-2024-01")
    assertThat(balance(JAN, CAT1)).isEqualTo(Zero)
    assertThat(note(JAN)).isEqualTo("- Reassigned 20.00 from To Budget → cat1 on February 15")
  }

  @Test
  fun `Nothing is covered when the category isn't overspent`() = runWriterTest {
    writer.setBudget(JAN, CAT1, Amount(1000))
    writer.setBudget(JAN, CAT2, Amount(500))
    syncCalls.clear()

    writer.coverOverspending(JAN, to = CAT1, from = CAT2)

    assertThat(syncCalls).isEmpty()
  }

  @Test
  fun `Holding for next month is clamped to what's available`() = runWriterTest {
    insertTransaction("income", 20240110, amount = 10000, category = INCOME)

    assertThat(writer.holdForNextMonth(JAN, Amount(15000))).isTrue()

    assertThat(syncCalls.single())
      .containsExactly(
        LocalChange(ZERO_BUDGET_MONTHS, "2024-01", "buffered", MessageValue.Number(10000)),
      )
    assertThat(envelope(JAN)).all {
      prop(BudgetMonth.Envelope::buffered).isEqualTo(Amount(10000))
      prop(BudgetMonth.Envelope::toBudget).isEqualTo(Zero)
    }
  }

  @Test
  fun `Nothing is held when there's nothing to budget`() = runWriterTest {
    assertThat(writer.holdForNextMonth(JAN, Amount(15000))).isFalse()

    assertThat(syncCalls).isEmpty()
  }

  @Test
  fun `Resetting a hold`() = runWriterTest {
    insertTransaction("income", 20240110, amount = 10000, category = INCOME)
    writer.holdForNextMonth(JAN, Amount(4000))

    writer.resetHold(JAN)

    assertThat(envelope(JAN)).all {
      prop(BudgetMonth.Envelope::buffered).isEqualTo(Zero)
      prop(BudgetMonth.Envelope::toBudget).isEqualTo(Amount(10000))
    }
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

    private val notesDao = NotesDao(database)
    val calculator = BudgetMonthCalculatorImpl(budgetDao, preferences, calendar, contexts)
    val writer =
      BudgetWriterImpl(
        syncController = this,
        budgetDao = budgetDao,
        preferencesDao = preferences,
        calendar = calendar,
        calculator = calculator,
        notesDao = notesDao,
        formatPreferences = TestFormatPreferences,
        currencyPreferences = TestCurrencyPreferences,
      )

    override suspend fun syncChanges(changes: List<LocalChange>) {
      syncCalls.add(changes)
      syncDao.sendMessages(changes)
    }

    override fun schedule() = Unit

    suspend fun budgeted(month: YearMonth, category: CategoryId): Amount =
      calculator.observe(month).first().categories.single { it.id == category }.budgeted

    suspend fun envelope(month: YearMonth): BudgetMonth.Envelope =
      calculator.observe(month).first() as BudgetMonth.Envelope

    suspend fun balance(month: YearMonth, category: CategoryId): Amount =
      envelope(month).categories.single { it.id == category }.balance

    suspend fun note(month: YearMonth): String? = notesDao.getNote("budget-$month")

    // prepareDatabase(). February is 90 overbudgeted, with balances of 100, -20 and 10
    suspend fun insertOverbudgeted() {
      run(
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) " +
          "VALUES ('cat3', 'cat3', 0, 'group1', 3)",
      )
      run("INSERT INTO category_mapping(id, transferId) VALUES ('cat3', 'cat3')")
      writer.setBudget(JAN, CAT1, Amount(100))
      writer.setBudget(JAN, CAT2, Amount(-20))
      writer.setBudget(JAN, CAT3, Amount(10))
      writer.setCarryover(JAN, CAT2, enabled = true)
    }

    // cat1 is overspent by 2000 and cat2 has 500 left
    suspend fun insertOverspending() {
      writer.setBudget(JAN, CAT1, Amount(1000))
      writer.setBudget(JAN, CAT2, Amount(500))
      insertTransaction("spend", 20240120, amount = -3000)
    }

    suspend fun tracking() {
      preferences[SyncedPrefKey.Global.BudgetType] = "tracking"
    }

    suspend fun hideCategory(id: CategoryId) =
      run("UPDATE categories SET hidden = 1 WHERE id = '${id.value}'")

    suspend fun insertTransaction(
      id: String,
      date: Int,
      amount: Int,
      category: CategoryId = CAT1,
    ) =
      run(
        "INSERT INTO transactions(id, acct, category, amount, date, tombstone) " +
          "VALUES ('$id', 'account1', '${category.value}', $amount, $date, 0)",
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

  private object TestFormatPreferences : FormatPreferences {
    override val hideFraction = fixed(false)
    override val dateFormat = fixed(DateFormat.Default)
    override val firstDayOfWeek = fixed(FirstDayOfWeek.Default)
    override val numberFormat = fixed(NumberFormat.Default)
  }

  private object TestCurrencyPreferences : CurrencyPreferences {
    override val currency = fixed(Currency.UsDollar)
    override val symbolPosition = fixed(CurrencySymbolPosition.BeforeAmount)
    override val spaceBetweenAmountAndSymbol = fixed(false)
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
    val CAT3 = CategoryId("cat3")
    val INCOME = CategoryId("income-cat")

    fun <T : Any> fixed(value: T) =
      object : Preference<T> {
        override val default = value

        override suspend fun get() = value

        override suspend fun set(value: T?) = Unit

        override fun asFlow() = flowOf(value)
      }

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
