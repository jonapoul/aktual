package aktual.budget.budgeting.vm

import aktual.budget.SyncStateHolder
import aktual.budget.budgeting.domain.BudgetMonthCalculatorImpl
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.Amount
import aktual.budget.model.BudgetId
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.DbMetadata
import aktual.budget.model.SyncedPrefKey
import aktual.test.TestBudgetLocalPreferences
import aktual.test.TestCalendar
import aktual.test.TestSyncController
import aktual.test.inMemoryDriverFactory
import alakazam.test.TestCoroutineContexts
import app.cash.sqldelight.db.SqlDriver
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
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
class BudgetViewModelTest {
  private val calendar = TestCalendar(LocalDate(2026, 4, 15))
  private val prefs = TestBudgetLocalPreferences(DbMetadata())

  private val BudgetState.Loaded.shown: MonthBudget
    get() = checkNotNull(this[month]) { "$month hasn't loaded" }

  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Loads the envelope budget for the current month`() = runBudgetTest { viewModel, _ ->
    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(Loading)

      val state = awaitLoaded()
      assertThat(state.type).isEqualTo(Envelope)
      assertThat(state.month).isEqualTo(YearMonth(2026, 4))
      assertThat(state.shown.summary)
        .isEqualTo(
          BudgetSummary.Envelope(
            toBudget = Amount(-5_000L),
            available = Amount(300_000L),
            budgeted = Amount(305_000L),
          ),
        )
      assertThat(state.shown.groups.map { it.id }).containsExactly(CategoryGroupId("usual"))
      assertThat(state.shown.groups.single().categories)
        .containsExactly(
          CategoryRow(
            id = CategoryId("food"),
            name = "Food",
            isHidden = false,
            budgeted = Amount(5_000L),
            spent = Amount(-50_000L),
            balance = Amount(-45_000L),
            carryover = false,
          ),
          CategoryRow(
            id = CategoryId("rent"),
            name = "Rent",
            isHidden = false,
            budgeted = Amount(300_000L),
            spent = Zero,
            balance = Amount(300_000L),
            carryover = false,
          ),
        )
      assertThat(state.shown.income?.categories?.map { it.spent })
        .isEqualTo(listOf(Amount(300_000L)))
      assertThat(state.showSpent).isFalse()
      assertThat(state.showHidden).isFalse()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Shows the month from the route`() =
    runBudgetTest(month = YearMonth(2026, 3)) { viewModel, _ ->
      viewModel.state.test {
        assertThat(awaitLoaded().month).isEqualTo(YearMonth(2026, 3))
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Banners for uncategorised, overspent and overbudgeted`() = runBudgetTest { viewModel, _ ->
    viewModel.state.test {
      assertThat(awaitLoaded().shown.banners)
        .containsExactly(
          Banner.Uncategorised(count = 1),
          Banner.Overspent(count = 1, total = Amount(-45_000L)),
          Banner.Overbudgeted(Amount(-5_000L)),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Toggles the spent column`() = runBudgetTest { viewModel, _ ->
    viewModel.state.test {
      assertThat(awaitLoaded().showSpent).isFalse()

      viewModel.toggleSpent()
      assertThat(awaitLoaded().showSpent).isTrue()
      assertThat(prefs.value[DbMetadata.MobileShowSpentColumn]).isEqualTo(true)

      viewModel.toggleSpent()
      assertThat(awaitLoaded().showSpent).isFalse()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Collapses and expands a group`() = runBudgetTest { viewModel, _ ->
    viewModel.state.test {
      assertThat(awaitLoaded().shown.groups.single().isCollapsed).isFalse()

      viewModel.toggleCollapsed(CategoryGroupId("usual"))
      assertThat(awaitLoaded().shown.groups.single().isCollapsed).isTrue()
      assertThat(prefs.value[DbMetadata.BudgetCollapsed]).isEqualTo(listOf("usual"))

      viewModel.toggleCollapsed(CategoryGroupId("usual"))
      assertThat(awaitLoaded().shown.groups.single().isCollapsed).isFalse()
      assertThat(prefs.value[DbMetadata.BudgetCollapsed]).isEqualTo(emptyList())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Hidden categories and groups only show when asked for`() =
    runBudgetTest(extra = HIDDEN) { viewModel, _ ->
      viewModel.state.test {
        val hidden = awaitLoaded().shown
        assertThat(hidden.groups.map { it.id }).containsExactly(CategoryGroupId("usual"))
        assertThat(hidden.groups.single().categories.map { it.id })
          .containsExactly(CategoryId("food"), CategoryId("rent"))

        viewModel.toggleHidden()
        val state = awaitLoaded()
        assertThat(state.showHidden).isTrue()
        val shown = state.shown
        assertThat(shown.groups.map { it.id })
          .containsExactly(CategoryGroupId("usual"), CategoryGroupId("old"))
        assertThat(shown.groups[0].categories.map { it.id to it.isHidden })
          .containsExactly(
            CategoryId("food") to false,
            CategoryId("rent") to false,
            CategoryId("gym") to true,
          )
        // Categories in a hidden group are shown as hidden too
        assertThat(shown.groups[1].categories.map { it.isHidden }).containsExactly(true)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Tracking budgets project savings for months that haven't finished`() =
    runBudgetTest(extra = TRACKING) { viewModel, database ->
      database.preferences(this)[SyncedPrefKey.Global.BudgetType] = "tracking"

      viewModel.state.test {
        var state = awaitLoaded()
        while (state.type != Tracking) state = awaitLoaded()
        assertThat(state.shown.summary)
          .isEqualTo(
            BudgetSummary.Tracking(
              saved = Amount(280_000L - 305_000L),
              isProjected = true,
              budgeted = Amount(305_000L),
              spent = Amount(-50_000L),
            ),
          )
        // Overspending isn't flagged as overbudgeted for tracking budgets
        assertThat(state.shown.banners.filterIsInstance<Banner.Overbudgeted>()).containsExactly()
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Tracking budgets show what was saved in past months`() =
    runBudgetTest(month = YearMonth(2026, 3), extra = TRACKING) { viewModel, database ->
      database.preferences(this)[SyncedPrefKey.Global.BudgetType] = "tracking"

      viewModel.state.test {
        var state = awaitLoaded()
        while (state.type != Tracking) state = awaitLoaded()
        assertThat(state.shown.summary)
          .isInstanceOf<BudgetSummary.Tracking>()
          .prop(BudgetSummary.Tracking::isProjected)
          .isFalse()
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Paging moves the month`() = runBudgetTest { viewModel, _ ->
    viewModel.state.test {
      assertThat(awaitLoaded().month).isEqualTo(YearMonth(2026, 4))

      // Already prefetched, so there's no wait for it to load
      viewModel.nextMonth()
      assertThat(awaitLoaded().shown.month).isEqualTo(YearMonth(2026, 5))

      viewModel.previousMonth()
      awaitMonth(YearMonth(2026, 4))

      viewModel.showMonth(YearMonth(2026, 8))
      awaitMonth(YearMonth(2026, 8))

      viewModel.showToday()
      awaitMonth(YearMonth(2026, 4))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Loads the months either side`() = runBudgetTest { viewModel, _ ->
    viewModel.state.test {
      assertThat(awaitLoaded().months.map { it.month })
        .containsExactly(YearMonth(2026, 3), YearMonth(2026, 4), YearMonth(2026, 5))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Follows the current month`() = runBudgetTest { viewModel, _ ->
    viewModel.state.test {
      assertThat(awaitLoaded().month).isEqualTo(YearMonth(2026, 4))

      calendar.set(LocalDate(2026, 5, 1))
      assertThat(awaitMonth(YearMonth(2026, 5)).current).isEqualTo(YearMonth(2026, 5))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Months stay within the budget bounds`() = runBudgetTest { viewModel, _ ->
    viewModel.state.test {
      val initial = awaitLoaded()
      assertThat(initial).all {
        prop(BudgetState.Loaded::earliest).isEqualTo(YearMonth(2026, 1))
        prop(BudgetState.Loaded::latest).isEqualTo(YearMonth(2027, 4))
        prop(BudgetState.Loaded::canGoBack).isTrue()
        prop(BudgetState.Loaded::canGoForward).isTrue()
      }

      viewModel.showMonth(YearMonth(2030, 1))
      assertThat(awaitMonth(YearMonth(2027, 4))).all {
        prop(BudgetState.Loaded::canGoBack).isTrue()
        prop(BudgetState.Loaded::canGoForward).isFalse()
      }

      viewModel.showMonth(YearMonth(2020, 1))
      assertThat(awaitMonth(YearMonth(2026, 1))).all {
        prop(BudgetState.Loaded::canGoBack).isFalse()
        prop(BudgetState.Loaded::canGoForward).isTrue()
      }

      viewModel.previousMonth()
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Month count is limited to what fits`() = runBudgetTest { viewModel, _ ->
    viewModel.state.test {
      assertThat(awaitLoaded().monthCount).isEqualTo(1)

      viewModel.setMonthCount(3)
      assertThat(prefs.value[DbMetadata.BudgetMonthCount]).isEqualTo(3)
      expectNoEvents()

      viewModel.setFittingMonths(2)
      val two = awaitLoaded()
      assertThat(two.monthCount).isEqualTo(2)
      assertThat(two.maxMonthCount).isEqualTo(2)
      assertThat(two.lastMonth).isEqualTo(YearMonth(2026, 5))

      viewModel.setFittingMonths(9)
      var three = awaitLoaded()
      while (three.months.size < 5) three = awaitLoaded()
      assertThat(three.monthCount).isEqualTo(3)
      assertThat(three.maxMonthCount).isEqualTo(4)
      assertThat(three.months.map { it.month })
        .containsExactly(
          YearMonth(2026, 3),
          YearMonth(2026, 4),
          YearMonth(2026, 5),
          YearMonth(2026, 6),
          YearMonth(2026, 7),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `The last months stay visible at the end of the budget`() = runBudgetTest { viewModel, _ ->
    viewModel.setMonthCount(4)
    viewModel.setFittingMonths(4)
    viewModel.showMonth(YearMonth(2030, 1))

    viewModel.state.test {
      val state = awaitMonth(YearMonth(2027, 1))
      assertThat(state.lastMonth).isEqualTo(YearMonth(2027, 4))
      assertThat(state.canGoForward).isFalse()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Fails when the database can't be read`() = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("closed"))
    driver.close()
    val viewModel = buildDatabase(driver).createViewModel(this, month = null)
    viewModel.state.test {
      var state = awaitItem()
      while (state == Loading) state = awaitItem()
      assertThat(state).isEqualTo(Failed)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Nothing to show without categories`() = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("empty"))
    driver.use {
      val viewModel = buildDatabase(driver).createViewModel(this, month = null)
      viewModel.state.test {
        val state = awaitLoaded()
        assertThat(state.isEmpty).isTrue()
        assertThat(state.shown.income).isNull()
        cancelAndIgnoreRemainingEvents()
      }
    }
  }

  private suspend fun ReceiveTurbine<BudgetState>.awaitLoaded(): BudgetState.Loaded {
    var state = awaitItem()
    while (state !is Loaded) state = awaitItem()
    return state
  }

  // Waits for the month to be shown and loaded
  private suspend fun ReceiveTurbine<BudgetState>.awaitMonth(month: YearMonth): BudgetState.Loaded {
    var state = awaitLoaded()
    while (state.month != month || state[month] == null) state = awaitLoaded()
    return state
  }

  private fun runBudgetTest(
    month: YearMonth? = null,
    extra: List<String> = emptyList(),
    action: suspend TestScope.(BudgetViewModel, BudgetDatabase) -> Unit,
  ) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use {
      val database = buildDatabase(driver)
      (SETUP + extra).forEach { sql -> driver.run(sql) }
      action(database.createViewModel(this, month), database)
    }
  }

  private fun BudgetDatabase.createViewModel(scope: TestScope, month: YearMonth?): BudgetViewModel {
    Dispatchers.setMain(StandardTestDispatcher(scope.testScheduler))
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
    return BudgetViewModel(
      month = month,
      calculator =
        BudgetMonthCalculatorImpl(
          budgetDao = BudgetDao(this, contexts),
          preferencesDao = preferences(scope),
          calendar = calendar,
          contexts = contexts,
        ),
      transactionDao = TransactionDao(this),
      localPreferences = prefs,
      syncController = TestSyncController(),
      calendar = calendar,
      syncStateHolder = SyncStateHolder(),
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
        "INSERT INTO category_groups(id, name, is_income, sort_order) VALUES ('income', 'Income', 1, 3)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('food', 'Food', 0, 'usual', 1)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('rent', 'Rent', 0, 'usual', 2)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('salary', 'Salary', 1, 'income', 1)",
        "INSERT INTO category_mapping(id, transferId) VALUES ('food', 'food'), ('rent', 'rent'), ('salary', 'salary')",
        "INSERT INTO accounts(id, name, offbudget) VALUES ('on', 'On', 0)",
        "INSERT INTO transactions(id, acct, category, amount, date) VALUES ('t1', 'on', 'salary', 300000, 20260401)",
        "INSERT INTO transactions(id, acct, category, amount, date) VALUES ('t2', 'on', 'food', -50000, 20260402)",
        "INSERT INTO transactions(id, acct, amount, date) VALUES ('t3', 'on', -1000, 20260403)",
        "INSERT INTO zero_budgets(id, month, category, amount) VALUES ('202604-food', 202604, 'food', 5000), ('202604-rent', 202604, 'rent', 300000)",
      )

    @Suppress("MaxLineLength")
    val HIDDEN =
      listOf(
        "INSERT INTO category_groups(id, name, is_income, sort_order, hidden) VALUES ('old', 'Old', 0, 2, 1)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order, hidden) VALUES ('gym', 'Gym', 0, 'usual', 3, 1)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('club', 'Club', 0, 'old', 1)",
        "INSERT INTO category_mapping(id, transferId) VALUES ('gym', 'gym'), ('club', 'club')",
      )

    @Suppress("MaxLineLength")
    val TRACKING =
      listOf(
        "INSERT INTO reflect_budgets(id, month, category, amount) VALUES ('202604-food', 202604, 'food', 5000), ('202604-rent', 202604, 'rent', 300000), ('202604-salary', 202604, 'salary', 280000)",
      )
  }
}
