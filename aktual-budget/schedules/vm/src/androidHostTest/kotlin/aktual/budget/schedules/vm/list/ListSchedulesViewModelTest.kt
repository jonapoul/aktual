package aktual.budget.schedules.vm.list

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.db.dao.DatabaseTables.SCHEDULES
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.model.LocalChange
import aktual.budget.model.tombstone
import aktual.budget.model.untombstone
import aktual.budget.schedules.vm.Schedule
import aktual.budget.schedules.vm.SchedulesLoader
import aktual.budget.schedules.vm.insertSchedule
import aktual.core.Calendar
import aktual.test.TestSyncController
import aktual.test.runDatabaseTest
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import kotlin.test.AfterTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ListSchedulesViewModelTest {
  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

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

      assertThat(awaitItem()).isEqualTo(ListSchedulesEvent.Deleted(rent, index = 0))
      assertThat(sync.changes)
        .containsExactly(tombstone(RULES, "a-rule"), tombstone(SCHEDULES, "a"))
    }
    assertThat(viewModel.state.awaitSchedules()).extracting(Schedule::name).containsExactly("Power")
  }

  @Test
  fun `Undo restores the schedule in its old position`() = runDatabaseTest { scope ->
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

    viewModel.undoDelete(rent, index = 0)
    scope.advanceUntilIdle()

    assertThat(sync.changes)
      .containsExactly(untombstone(RULES, "a-rule"), untombstone(SCHEDULES, "a"))
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
  ): ListSchedulesViewModel {
    Dispatchers.setMain(StandardTestDispatcher(scope.testScheduler))
    val loader =
      SchedulesLoader(
        scheduleDao = ScheduleDao(this),
        accountDao = AccountDao(this),
        payeeDao = PayeeDao(this),
        calendar = Calendar { LocalDate(2026, 4, 1) },
      )
    return ListSchedulesViewModel(loader = loader, syncController = sync)
  }

  private object FailingSyncController : BudgetSyncController {
    override suspend fun syncChanges(changes: List<LocalChange>): Unit = error("sync failed")

    override fun schedule() = Unit
  }
}
