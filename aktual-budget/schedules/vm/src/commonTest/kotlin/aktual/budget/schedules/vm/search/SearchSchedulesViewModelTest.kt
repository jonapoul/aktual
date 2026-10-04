package aktual.budget.schedules.vm.search

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.model.ScheduleId
import aktual.budget.schedules.domain.Schedule
import aktual.budget.schedules.domain.SchedulesLoader
import aktual.budget.schedules.vm.search.SearchSchedulesState.Failure
import aktual.budget.schedules.vm.search.SearchSchedulesState.Results
import aktual.core.Calendar
import aktual.test.insertSchedule
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

class SearchSchedulesViewModelTest {
  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Blank query prompts for input`() = runDatabaseTest { scope ->
    insertSchedule(id = "rent", name = "Rent", payee = "Landlord", account = "Checking")
    val viewModel = createViewModel(scope)

    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(NoQuery)
      viewModel.setQuery("   ")
      scope.advanceUntilIdle()
      expectNoEvents()
    }
  }

  @Test
  fun `Matches name, payee and account case-insensitively`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Monthly RENT", payee = "Landlord", account = "Checking")
    insertSchedule(id = "b", name = "Power", payee = "Rentokil", account = "Checking")
    insertSchedule(id = "c", name = null, payee = "Gym", account = "Rent account")
    insertSchedule(id = "d", name = "Phone", payee = "Telco", account = "Savings")
    val viewModel = createViewModel(scope)

    viewModel.setQuery(" rent ")

    viewModel.state.test {
      assertThat(awaitResults())
        .extracting(Schedule::payeeName)
        .containsExactly("Landlord", "Rentokil", "Gym")
    }
  }

  @Test
  fun `No matches shows no results`() = runDatabaseTest { scope ->
    insertSchedule(id = "rent", name = "Rent", payee = "Landlord", account = "Checking")
    val viewModel = createViewModel(scope)

    viewModel.setQuery("xyz")

    viewModel.state.test {
      var state = awaitItem()
      while (state != NoResults) state = awaitItem()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Load failure shows failure, and reload recovers`() = runDatabaseTest { scope ->
    // Pointing the date path at the amount condition makes the date unparseable
    insertSchedule(
      id = "broken",
      name = "Broken",
      payee = "Landlord",
      account = "Checking",
      dateConditionIndex = 3,
    )
    val viewModel = createViewModel(scope)
    viewModel.setQuery("broken")

    viewModel.state.test {
      var state = awaitItem()
      while (state !is Failure) state = awaitItem()
      assertThat(state).isInstanceOf(Failure::class)

      schedulesQueries.delete(ScheduleId("broken"))
      insertSchedule(id = "fixed", name = "Broken fixed", payee = "Landlord", account = "Checking")
      viewModel.reload()

      assertThat(awaitResults()).extracting(Schedule::name).containsExactly("Broken fixed")
    }
  }

  private suspend fun ReceiveTurbine<SearchSchedulesState>.awaitResults(): List<Schedule> {
    var state = awaitItem()
    while (state !is Results) state = awaitItem()
    cancelAndIgnoreRemainingEvents()
    return state.schedules
  }

  private fun BudgetDatabase.createViewModel(scope: TestScope): SearchSchedulesViewModel {
    val dispatcher = StandardTestDispatcher(scope.testScheduler)
    Dispatchers.setMain(dispatcher)
    val loader =
      SchedulesLoader(
        scheduleDao = ScheduleDao(this),
        accountDao = AccountDao(this),
        payeeDao = PayeeDao(this),
        preferencesDao = PreferencesDao(this, TestCoroutineContexts(dispatcher)),
        calendar = Calendar { LocalDate(2026, 4, 1) },
      )
    return SearchSchedulesViewModel(savedState = SavedStateHandle(), loader = loader)
  }
}
