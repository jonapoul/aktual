package aktual.budget.home.vm

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.withoutResult
import aktual.budget.home.vm.AccountsCardState.Loaded
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.DbMetadata
import aktual.budget.model.SyncedPrefKey.Global.UpcomingScheduledTransactionLength
import aktual.budget.model.UpcomingLength
import aktual.test.TestBudgetLocalPreferences
import aktual.test.TestCalendar
import aktual.test.insertSchedule
import aktual.test.runDatabaseTest
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.datetime.LocalDate
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeViewModelTest {
  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Budget name follows local preferences`() = runDatabaseTest { scope ->
    val prefs = TestBudgetLocalPreferences(DbMetadata(budgetName = "Household"))
    val viewModel = createHomeViewModel(scope, CALENDAR, prefs)

    viewModel.state.test {
      assertThat(awaitSettled().budgetName).isEqualTo("Household")

      prefs += DbMetadata(budgetName = "Renamed")
      var state = awaitItem()
      while (state.budgetName != "Renamed") state = awaitItem()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A new budget has nothing to show`() = runDatabaseTest { scope ->
    val viewModel = createHomeViewModel(scope, CALENDAR)

    viewModel.state.test { assertThat(awaitSettled().isEmpty).isTrue() }
  }

  @Test
  fun `A budget with an account has something to show`() = runDatabaseTest { scope ->
    insertAccount("a")
    val viewModel = createHomeViewModel(scope, CALENDAR)

    viewModel.state.test { assertThat(awaitSettled().isEmpty).isFalse() }
  }

  @Test
  fun `A failed month isn't hidden behind the empty state`() {
    val state =
      HomeState(
        budgetName = null,
        thisMonth = Failed,
        attention = Empty,
        upcoming = Empty,
        accounts = Empty,
      )

    assertThat(state.isEmpty).isFalse()
  }

  @Test
  fun `Retrying doesn't put loaded cards back to loading`() = runDatabaseTest { scope ->
    insertAccount("a")
    val viewModel = createHomeViewModel(scope, CALENDAR)

    viewModel.state.test {
      awaitSettled()

      viewModel.retry()
      scope.testScheduler.advanceUntilIdle()
      expectNoEvents()
    }
  }

  @Test
  fun `A failing source only fails its own card`() = runDatabaseTest { scope ->
    insertAccount("a")
    val viewModel = createHomeViewModel(scope, CALENDAR, accountsCard = closedDatabase())

    viewModel.state.test {
      assertThat(awaitSettled()).all {
        prop(HomeState::accounts).isEqualTo(AccountsCardState.Failed)
        prop(HomeState::thisMonth).isInstanceOf<ThisMonthCardState.Loaded>()
        prop(HomeState::attention).isEqualTo(Empty)
        prop(HomeState::upcoming).isEqualTo(Empty)
      }
    }
  }

  @Test
  fun `No open accounts is empty`() = runDatabaseTest { scope ->
    insertAccount("closed")
    accountsQueries.withoutResult { closeAccount(AccountId("closed")) }
    val viewModel = createHomeViewModel(scope, CALENDAR)

    viewModel.state.test { assertThat(awaitAccounts()).isEqualTo(Empty) }
  }

  @Test
  fun `Accounts summary updates when balances change`() = runDatabaseTest { scope ->
    insertAccount("a")
    insertAccount("b", offBudget = true)
    val transactions = TransactionDao(this)
    transactions.insert("t1", "a", "cat", "payee", DATE, amount = 10.0)
    val viewModel = createHomeViewModel(scope, CALENDAR)

    viewModel.state.test {
      assertThat(awaitLoaded()).hasBalances(a = 1_000L, b = 0L, netWorth = 1_000L)

      transactions.insert("t2", "b", "cat", "payee", DATE, amount = -25.0)
      assertThat(awaitLoaded()).hasBalances(a = 1_000L, b = -2_500L, netWorth = -1_500L)

      transactions.insert("t3", "a", "cat", "payee", DATE, amount = 5.5)
      assertThat(awaitLoaded()).hasBalances(a = 1_550L, b = -2_500L, netWorth = -950L)
    }
  }

  @Test
  fun `Accounts collapse to the most recently active`() = runDatabaseTest { scope ->
    val transactions = TransactionDao(this)
    for (day in 1..7) {
      insertAccount("a$day")
      transactions.insert("t$day", "a$day", "cat", "payee", date(day), amount = 1.0)
    }
    val viewModel = createHomeViewModel(scope, CALENDAR)

    viewModel.state.test {
      assertThat(awaitLoaded().recent?.onBudget?.accounts?.map { it.id.value })
        .isEqualTo(listOf("a3", "a4", "a5", "a6", "a7"))
    }
  }

  @Test
  fun `Few accounts have nothing to collapse`() = runDatabaseTest { scope ->
    insertAccount("a")
    val viewModel = createHomeViewModel(scope, CALENDAR)

    viewModel.state.test {
      assertThat(awaitLoaded().recent).isEqualTo(null)
    }
  }

  @Test
  fun `Nothing upcoming is empty`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Later", payee = "p", account = "a", nextDate = date(9))
    val viewModel = createHomeViewModel(scope, CALENDAR)

    viewModel.state.test { assertThat(awaitUpcoming()).isEqualTo(Empty) }
  }

  @Test
  fun `Upcoming includes missed and due but not completed or paid`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Upcoming", payee = "p", account = "a", nextDate = date(5))
    insertSchedule(id = "b", name = "Due", payee = "p", account = "a", nextDate = TODAY)
    insertSchedule(id = "c", name = "Missed", payee = "p", account = "a", nextDate = MISSED)
    insertSchedule(
      id = "d",
      name = "Completed",
      payee = "p",
      account = "a",
      nextDate = date(2),
      completed = true,
    )
    insertSchedule(id = "e", name = "Paid", payee = "p", account = "a", nextDate = date(3))
    TransactionDao(this).insert("t1", "e-account", "cat", "e-payee", date(3), schedule = "e")
    val viewModel = createHomeViewModel(scope, CALENDAR)

    viewModel.state.test {
      assertThat(awaitUpcomingLoaded()).all {
        hasNames("Missed", "Due", "Upcoming")
        prop(UpcomingCardState.Loaded::today).isEqualTo(TODAY)
        prop(UpcomingCardState.Loaded::hiddenCount).isEqualTo(0)
        prop(UpcomingCardState.Loaded::total).isEqualTo(Amount(-3_000L))
      }
    }
  }

  @Test
  fun `Upcoming window follows the global length`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Inside", payee = "p", account = "a", nextDate = date(8))
    insertSchedule(id = "b", name = "Outside", payee = "p", account = "a", nextDate = date(9))
    val viewModel = createHomeViewModel(scope, CALENDAR)

    viewModel.state.test {
      assertThat(awaitUpcomingLoaded()).all {
        hasNames("Inside")
        prop(UpcomingCardState.Loaded::length).isEqualTo(UpcomingLength.Days(count = 7))
      }

      val twoWeeks = UpcomingLength.Weeks(count = 2)
      preferences(scope)[UpcomingScheduledTransactionLength] = twoWeeks.encode()
      var loaded = awaitUpcomingLoaded()
      while (loaded.schedules.size < 2 || loaded.length != twoWeeks) loaded = awaitUpcomingLoaded()
      assertThat(loaded).hasNames("Inside", "Outside")
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Upcoming rows are capped but the total isn't`() = runDatabaseTest { scope ->
    for (day in 2..8) {
      insertSchedule(id = "s$day", name = "S$day", payee = "p", account = "a", nextDate = date(day))
    }
    val viewModel = createHomeViewModel(scope, CALENDAR)

    viewModel.state.test {
      assertThat(awaitUpcomingLoaded()).all {
        hasNames("S2", "S3", "S4", "S5", "S6")
        prop(UpcomingCardState.Loaded::hiddenCount).isEqualTo(2)
        prop(UpcomingCardState.Loaded::total).isEqualTo(Amount(-7_000L))
      }
    }
  }

  private fun Assert<UpcomingCardState.Loaded>.hasNames(vararg names: String) = transform {
    it.schedules.map { schedule -> schedule.name }
  }
    .containsExactly(*names)

  private suspend fun ReceiveTurbine<HomeState>.awaitUpcoming() = awaitSettled().upcoming

  private suspend fun ReceiveTurbine<HomeState>.awaitUpcomingLoaded() =
    awaitUpcoming() as UpcomingCardState.Loaded

  private fun Assert<Loaded>.hasBalances(a: Long, b: Long, netWorth: Long) =
    prop(Loaded::summary).all {
      transform { it.onBudget.accounts.map { account -> account.balance } }
        .containsExactly(Amount(a))
      transform { it.offBudget.accounts.map { account -> account.balance } }
        .containsExactly(Amount(b))
      transform { it.netWorth }.isEqualTo(Amount(netWorth))
    }

  private suspend fun ReceiveTurbine<HomeState>.awaitAccounts() = awaitSettled().accounts

  private suspend fun ReceiveTurbine<HomeState>.awaitLoaded() = awaitAccounts() as Loaded

  private suspend fun BudgetDatabase.insertAccount(id: String, offBudget: Boolean = false) =
    accountsQueries.withoutResult {
      insert(
        id = AccountId(id),
        account_id = null,
        name = id,
        official_name = null,
        bank = null,
        offbudget = offBudget,
        account_sync_source = null,
      )
    }

  private companion object {
    val DATE = LocalDate(2026, 1, 1)
    val TODAY = LocalDate(2026, 4, 1)
    val MISSED = LocalDate(2026, 3, 20)
    val CALENDAR = TestCalendar(TODAY)

    fun date(day: Int) = LocalDate(2026, 4, day)
  }
}
