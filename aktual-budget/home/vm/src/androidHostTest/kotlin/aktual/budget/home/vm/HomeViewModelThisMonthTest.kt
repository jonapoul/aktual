package aktual.budget.home.vm

import aktual.budget.budgeting.domain.BudgetMonthCalculatorImpl
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.home.domain.AccountsSummaryLoader
import aktual.budget.home.domain.ThisMonthLoader
import aktual.budget.home.domain.UpcomingSchedulesLoader
import aktual.budget.model.Amount
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.SyncedPrefKey
import aktual.budget.schedules.domain.SchedulesLoader
import aktual.core.Calendar
import aktual.test.TestBudgetLocalPreferences
import aktual.test.inMemoryDriverFactory
import alakazam.test.TestCoroutineContexts
import app.cash.sqldelight.db.SqlDriver
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeViewModelThisMonthTest {
  private var today = LocalDate(2026, 4, 15)

  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Envelope with money left to budget`() =
    runThisMonthTest(envelopeBudget(30_000)) { viewModel, _ ->
      viewModel.state.test {
        assertThat(awaitThisMonth()).isEqualTo(envelope(budgeted = 30_000L, toBudget = 270_000L))
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Envelope with everything budgeted`() =
    runThisMonthTest(envelopeBudget(300_000)) { viewModel, _ ->
      viewModel.state.test {
        assertThat(awaitThisMonth()).isEqualTo(envelope(budgeted = 300_000L, toBudget = 0L))
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Envelope that's overbudgeted`() =
    runThisMonthTest(envelopeBudget(350_000)) { viewModel, _ ->
      viewModel.state.test {
        assertThat(awaitThisMonth()).isEqualTo(envelope(budgeted = 350_000L, toBudget = -50_000L))
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Tracking budget shows what remains and the income`() =
    runThisMonthTest(TRACKING_BUDGET) { viewModel, database ->
      database.preferences(this)[SyncedPrefKey.Global.BudgetType] = "tracking"

      viewModel.state.test {
        val expected =
          ThisMonthCardState.Tracking(
            month = YearMonth(2026, 4),
            daysLeft = 15,
            spent = Amount(50_000L),
            budgeted = Amount(45_000L),
            income = Amount(300_000L),
            incomeBudgeted = Amount(280_000L),
          )
        var state = awaitThisMonth()
        while (state !is ThisMonthCardState.Tracking) state = awaitThisMonth()
        assertThat(state).isEqualTo(expected)
        assertThat(state.remaining).isEqualTo(Amount(-5_000L))
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Rolls over to the next month at midnight`() {
    today = LocalDate(2026, 4, 30)
    runThisMonthTest(envelopeBudget(30_000)) { viewModel, _ ->
      viewModel.state.test {
        assertThat(awaitThisMonth())
          .isEqualTo(envelope(budgeted = 30_000L, toBudget = 270_000L, daysLeft = 0))

        today = LocalDate(2026, 5, 1)
        var state = awaitThisMonth()
        while (state.month != YearMonth(2026, 5)) state = awaitThisMonth()
        assertThat(state)
          .isEqualTo(
            ThisMonthCardState.Envelope(
              month = YearMonth(2026, 5),
              daysLeft = 30,
              spent = Zero,
              budgeted = Zero,
              // April's overspend on food comes out of what was left
              toBudget = Amount(250_000L),
            )
          )
        cancelAndIgnoreRemainingEvents()
      }
    }
  }

  private fun envelope(budgeted: Long, toBudget: Long, daysLeft: Int = 15) =
    ThisMonthCardState.Envelope(
      month = YearMonth(2026, 4),
      daysLeft = daysLeft,
      spent = Amount(50_000L),
      budgeted = Amount(budgeted),
      toBudget = Amount(toBudget),
    )

  private suspend fun ReceiveTurbine<HomeState>.awaitThisMonth(): ThisMonthCardState.Loaded {
    var state = awaitItem()
    while (state.thisMonth == Loading) state = awaitItem()
    return state.thisMonth as ThisMonthCardState.Loaded
  }

  private fun runThisMonthTest(
    budget: String,
    action: suspend TestScope.(HomeViewModel, BudgetDatabase) -> Unit,
  ) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use {
      val database = buildDatabase(driver)
      (SETUP + budget).forEach { sql -> driver.run(sql) }
      action(database.createViewModel(this), database)
    }
  }

  private fun BudgetDatabase.createViewModel(scope: TestScope): HomeViewModel {
    Dispatchers.setMain(StandardTestDispatcher(scope.testScheduler))
    val calendar = Calendar { today }
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
    val calculator =
      BudgetMonthCalculatorImpl(
        budgetDao = BudgetDao(this, contexts),
        preferencesDao = preferences(scope),
        calendar = calendar,
        contexts = contexts,
      )
    val schedulesLoader =
      SchedulesLoader(
        scheduleDao = ScheduleDao(this),
        accountDao = AccountDao(this),
        payeeDao = PayeeDao(this),
        preferencesDao = preferences(scope),
        calendar = calendar,
      )
    return HomeViewModel(
      localPreferences = TestBudgetLocalPreferences(DbMetadata()),
      thisMonthLoader = ThisMonthLoader(calculator, calendar),
      accountsSummaryLoader = AccountsSummaryLoader(AccountDao(this)),
      upcomingSchedulesLoader = UpcomingSchedulesLoader(schedulesLoader, calendar),
    )
  }

  private fun BudgetDatabase.preferences(scope: TestScope) =
    PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))

  private suspend fun SqlDriver.run(sql: String) {
    execute(identifier = null, sql = sql, parameters = 0).await()
  }

  private companion object {
    @Suppress("MaxLineLength")
    val SETUP =
      listOf(
        "INSERT INTO category_groups(id, name, is_income, sort_order) VALUES ('usual', 'Usual', 0, 1)",
        "INSERT INTO category_groups(id, name, is_income, sort_order) VALUES ('income', 'Income', 1, 2)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('food', 'Food', 0, 'usual', 1)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('salary', 'Salary', 1, 'income', 1)",
        "INSERT INTO category_mapping(id, transferId) VALUES ('food', 'food'), ('salary', 'salary')",
        "INSERT INTO accounts(id, name, offbudget) VALUES ('on', 'On', 0)",
        "INSERT INTO transactions(id, acct, category, amount, date) VALUES ('t1', 'on', 'salary', 300000, 20260401)",
        "INSERT INTO transactions(id, acct, category, amount, date) VALUES ('t2', 'on', 'food', -50000, 20260402)",
      )

    const val TRACKING_BUDGET =
      "INSERT INTO reflect_budgets(id, month, category, amount) VALUES " +
        "('202604-food', 202604, 'food', 45000), ('202604-salary', 202604, 'salary', 280000)"

    fun envelopeBudget(amount: Int) =
      "INSERT INTO zero_budgets(id, month, category, amount) VALUES ('202604-food', 202604, 'food', $amount)"
  }
}
