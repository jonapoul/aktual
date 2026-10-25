package aktual.budget.schedules.vm.list

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.db.dao.DatabaseTables.SCHEDULES
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.model.LocalChange
import aktual.budget.model.ScheduleSort
import aktual.budget.model.tombstone
import aktual.budget.model.untombstone
import aktual.budget.schedules.domain.Schedule
import aktual.budget.schedules.domain.SchedulesLoader
import aktual.core.Calendar
import aktual.prefs.SchedulePreferences
import aktual.prefs.SchedulePreferencesImpl
import aktual.test.TestSyncController
import aktual.test.buildPreferences
import aktual.test.insertSchedule
import aktual.test.runDatabaseTest
import aktual.test.viewModelContext
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.datetime.LocalDate
import org.junit.Test

class ListSchedulesViewModelTest {
  @Test
  fun `Delete tombstones the schedule and its rule`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Checking")
    insertSchedule(
      id = "b",
      name = "Power",
      payee = "Energy",
      account = "Checking",
      nextDate = LocalDate(2026, 5, 2),
    )
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, sync)

    viewModel.events.test {
      val rent = viewModel.state.awaitSchedules().first { it.name == "Rent" }
      viewModel.delete(rent)
      scope.advanceUntilIdle()

      assertThat(awaitItem()).isEqualTo(ListSchedulesEvent.Deleted(rent))
      assertThat(sync.changes)
        .containsExactly(tombstone(RULES, "a-rule"), tombstone(SCHEDULES, "a"))
    }
  }

  @Test
  fun `Undo untombstones the schedule and its rule`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Checking")
    insertSchedule(
      id = "b",
      name = "Power",
      payee = "Energy",
      account = "Checking",
      nextDate = LocalDate(2026, 5, 2),
    )
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, sync)

    val rent = viewModel.state.awaitSchedules().first { it.name == "Rent" }
    viewModel.delete(rent)
    scope.advanceUntilIdle()
    sync.changes.clear()

    viewModel.undoDelete(rent)
    scope.advanceUntilIdle()

    assertThat(sync.changes)
      .containsExactly(untombstone(RULES, "a-rule"), untombstone(SCHEDULES, "a"))
  }

  @Test
  fun `Schedules written to the database show up without a reload`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Checking")
    val viewModel = createViewModel(scope, TestSyncController())
    assertThat(viewModel.state.awaitSchedules()).extracting(Schedule::name).containsExactly("Rent")

    insertSchedule(
      id = "b",
      name = "Power",
      payee = "Energy",
      account = "Checking",
      nextDate = LocalDate(2026, 5, 2),
    )
    scope.advanceUntilIdle()

    assertThat(viewModel.state.awaitSchedules())
      .extracting(Schedule::name)
      .containsExactly("Rent", "Power")
  }

  @Test
  fun `Failed delete keeps the schedule and reports it`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Checking")
    val viewModel = createViewModel(scope, FailingSyncController)

    viewModel.events.test {
      val rent = viewModel.state.awaitSchedules().single()
      viewModel.delete(rent)
      scope.advanceUntilIdle()

      assertThat(awaitItem()).isEqualTo(ListSchedulesEvent.DeleteFailed(rent))
    }
    assertThat(viewModel.state.awaitSchedules()).extracting(Schedule::name).containsExactly("Rent")
  }

  @Test
  fun `Completed schedules are hidden by default`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Checking")
    insertSchedule(id = "b", name = "Old", payee = "Gym", account = "Checking", completed = true)
    val viewModel = createViewModel(scope, TestSyncController())

    assertThat(viewModel.state.awaitSchedules()).extracting(Schedule::name).containsExactly("Rent")
  }

  @Test
  fun `Showing completed schedules includes them`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Checking")
    insertSchedule(
      id = "b",
      name = "Old",
      payee = "Gym",
      account = "Checking",
      nextDate = LocalDate(2026, 5, 2),
      completed = true,
    )
    val preferences = SchedulePreferencesImpl(scope.buildPreferences())
    val viewModel = createViewModel(scope, TestSyncController(), preferences)
    assertThat(viewModel.state.awaitSchedules()).extracting(Schedule::name).containsExactly("Rent")

    preferences.showCompleted.set(true)
    scope.advanceUntilIdle()

    assertThat(viewModel.state.awaitSchedules())
      .extracting(Schedule::name)
      .containsExactly("Rent", "Old")
  }

  @Test
  fun `Only completed schedules shows empty when hidden`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Old", payee = "Gym", account = "Checking", completed = true)
    val viewModel = createViewModel(scope, TestSyncController())

    viewModel.state.test {
      var state = awaitItem()
      while (state == Loading) state = awaitItem()
      assertThat(state).isEqualTo(Empty)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Sorts by next date ascending by default`() = runDatabaseTest { scope ->
    insertSchedule(
      id = "a",
      name = "Rent",
      payee = "Landlord",
      account = "Checking",
      nextDate = LocalDate(2026, 5, 3),
    )
    insertSchedule(
      id = "b",
      name = "Power",
      payee = "Energy",
      account = "Checking",
      nextDate = LocalDate(2026, 5, 2),
    )
    val viewModel = createViewModel(scope, TestSyncController())

    viewModel.state.test {
      assertThat(awaitSuccess()).all {
        prop(Success::sort).isEqualTo(ScheduleSort(NextDate, Ascending))
        prop(Success::schedules).extracting(Schedule::name).containsExactly("Power", "Rent")
      }
    }
  }

  @Test
  fun `setSort persists the choice and re-sorts the list`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Checking", amount = -50)
    insertSchedule(id = "b", name = "Power", payee = "Energy", account = "Checking", amount = -10)
    insertSchedule(id = "c", name = null, payee = "Gym", account = "Checking", amount = -30)
    val preferences = SchedulePreferencesImpl(scope.buildPreferences())
    val viewModel = createViewModel(scope, TestSyncController(), preferences)
    viewModel.state.awaitSchedules()

    viewModel.setSort(ScheduleSort(Payee, Descending))
    scope.advanceUntilIdle()
    assertThat(viewModel.state.awaitSchedules())
      .extracting(Schedule::payeeName)
      .containsExactly("Landlord", "Gym", "Energy")
    assertThat(preferences.sortField.get()).isEqualTo(Payee)
    assertThat(preferences.sortDirection.get()).isEqualTo(Descending)

    viewModel.setSort(ScheduleSort(Amount, Ascending))
    scope.advanceUntilIdle()
    assertThat(viewModel.state.awaitSchedules())
      .extracting(Schedule::name)
      .containsExactly("Rent", null, "Power")

    viewModel.setSort(ScheduleSort(Name, Ascending))
    scope.advanceUntilIdle()
    assertThat(viewModel.state.awaitSchedules())
      .extracting(Schedule::name)
      .containsExactly("Power", "Rent", null)
  }

  private suspend fun StateFlow<ListSchedulesState>.awaitSchedules(): List<Schedule> {
    var schedules: List<Schedule> = emptyList()
    test { schedules = awaitSuccess().schedules }
    return schedules
  }

  private suspend fun ReceiveTurbine<ListSchedulesState>.awaitSuccess(): Success {
    var state = awaitItem()
    while (state !is Success) state = awaitItem()
    cancelAndIgnoreRemainingEvents()
    return state
  }

  private fun BudgetDatabase.createViewModel(
    scope: TestScope,
    sync: BudgetSyncController,
    preferences: SchedulePreferences = SchedulePreferencesImpl(scope.buildPreferences()),
  ): ListSchedulesViewModel {
    val dispatcher = StandardTestDispatcher(scope.testScheduler)
    val loader =
      SchedulesLoader(
        scheduleDao = ScheduleDao(this),
        accountDao = AccountDao(this),
        payeeDao = PayeeDao(this),
        preferencesDao = PreferencesDao(this, TestCoroutineContexts(dispatcher)),
        calendar = Calendar { LocalDate(2026, 4, 1) },
      )
    return ListSchedulesViewModel(
      loader = loader,
      syncController = sync,
      preferences = preferences,
      viewModelContext = scope.viewModelContext,
    )
  }

  private object FailingSyncController : BudgetSyncController {
    override suspend fun syncChanges(changes: List<LocalChange>): Unit = error("sync failed")

    override fun schedule() = Unit
  }
}
