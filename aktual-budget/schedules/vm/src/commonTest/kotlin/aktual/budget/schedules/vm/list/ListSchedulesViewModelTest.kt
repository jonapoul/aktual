package aktual.budget.schedules.vm.list

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.db.dao.DatabaseTables.SCHEDULES
import aktual.budget.db.dao.DatabaseTables.SCHEDULES_NEXT_DATE
import aktual.budget.db.dao.DatabaseTables.TRANSACTIONS
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.ScheduleSort
import aktual.budget.model.localChange
import aktual.budget.model.messageValue
import aktual.budget.model.tombstone
import aktual.budget.model.untombstone
import aktual.budget.rules.domain.TransactionRulesLoader
import aktual.budget.schedules.domain.Schedule
import aktual.budget.schedules.domain.ScheduleTransactionPoster
import aktual.budget.schedules.domain.SchedulesLoader
import aktual.budget.schedules.vm.edit.ScheduleWriter
import aktual.budget.transactions.domain.TransactionWriter
import aktual.core.Calendar
import aktual.core.UuidGenerator
import aktual.prefs.SchedulePreferences
import aktual.prefs.SchedulePreferencesImpl
import aktual.test.TestSyncController
import aktual.test.buildPreferences
import aktual.test.insertSchedule
import aktual.test.runDatabaseTest
import aktual.test.viewModelContext
import alakazam.test.TestClock
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.time.Instant
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
  fun `Complete marks the schedule completed`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Checking")
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, sync)

    viewModel.events.test {
      val rent = viewModel.state.awaitSchedules().single()
      viewModel.complete(rent)
      scope.advanceUntilIdle()

      assertThat(awaitItem()).isEqualTo(ListSchedulesEvent.Completed(rent))
      assertThat(sync.changes).containsExactly(localChange(SCHEDULES, "a", "completed", true))
    }
  }

  @Test
  fun `Undo complete uncompletes the schedule without moving its next date`() =
    runDatabaseTest { scope ->
      insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Checking")
      val sync = TestSyncController()
      val viewModel = createViewModel(scope, sync)

      val rent = viewModel.state.awaitSchedules().single()
      viewModel.complete(rent)
      scope.advanceUntilIdle()
      sync.changes.clear()

      viewModel.undoComplete(rent)
      scope.advanceUntilIdle()

      assertThat(sync.changes).containsExactly(localChange(SCHEDULES, "a", "completed", false))
    }

  @Test
  fun `Restart uncompletes the schedule and resets its next date from today`() =
    runDatabaseTest { scope ->
      insertSchedule(
        id = "a",
        name = "Rent",
        payee = "Landlord",
        account = "Checking",
        nextDate = LocalDate(2026, 1, 15),
        completed = true,
        date =
          """{"start": "2026-01-15", "frequency": "monthly", "interval": 1, "endMode": "never"}""",
      )
      val sync = TestSyncController()
      val preferences = SchedulePreferencesImpl(scope.buildPreferences())
      preferences.showCompleted.set(true)
      val viewModel = createViewModel(scope, sync, preferences)

      viewModel.restart(viewModel.state.awaitSchedules().single())
      scope.advanceUntilIdle()

      assertThat(sync.changes)
        .containsExactly(
          LocalChange(
            SCHEDULES_NEXT_DATE,
            "a-next",
            "base_next_date",
            LocalDate(2026, 4, 15).messageValue(),
          ),
          localChange(
            SCHEDULES_NEXT_DATE,
            "a-next",
            "base_next_date_ts",
            NOW.toEpochMilliseconds(),
          ),
          localChange(SCHEDULES, "a", "completed", false),
        )
    }

  @Test
  fun `Failed complete reports it`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Checking")
    val viewModel = createViewModel(scope, FailingSyncController)

    viewModel.events.test {
      val rent = viewModel.state.awaitSchedules().single()
      viewModel.complete(rent)
      scope.advanceUntilIdle()

      assertThat(awaitItem()).isEqualTo(ListSchedulesEvent.CompleteFailed(rent))
    }
  }

  @Test
  fun `Post adds the schedule's transaction on the chosen date`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Checking")
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, sync)

    viewModel.events.test {
      val rent = viewModel.state.awaitSchedules().single()
      viewModel.post(rent, TODAY.today())
      scope.advanceUntilIdle()

      val event = awaitItem()
      assertThat(event).isInstanceOf<ListSchedulesEvent.Posted>().all {
        prop(ListSchedulesEvent.Posted::schedule).isEqualTo(rent)
        prop(ListSchedulesEvent.Posted::transactions).hasSize(1)
      }
      assertThat(sync.posted("acct")).containsExactly(MessageValue.String("a-account"))
      assertThat(sync.posted("date")).containsExactly(MessageValue.Number(20260401))
      assertThat(sync.posted("amount")).containsExactly(MessageValue.Number(-1000))
      assertThat(sync.posted("schedule")).containsExactly(MessageValue.String("a"))
      assertThat(sync.posted("cleared")).containsExactly(MessageValue.Number(0))
    }
  }

  @Test
  fun `Undo post deletes the posted transaction`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Checking")
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, sync)

    viewModel.events.test {
      val rent = viewModel.state.awaitSchedules().single()
      viewModel.post(rent, rent.nextDate)
      scope.advanceUntilIdle()
      val posted = awaitItem() as Posted
      assertThat(sync.posted("date")).containsExactly(MessageValue.Number(20260501))
      sync.changes.clear()

      viewModel.undoPost(posted)
      scope.advanceUntilIdle()

      assertThat(sync.changes)
        .containsExactly(tombstone(TRANSACTIONS, posted.transactions.single().value))
    }
  }

  @Test
  fun `Failed post reports it`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Checking")
    val viewModel = createViewModel(scope, FailingSyncController)

    viewModel.events.test {
      val rent = viewModel.state.awaitSchedules().single()
      viewModel.post(rent, TODAY.today())
      scope.advanceUntilIdle()

      assertThat(awaitItem()).isEqualTo(ListSchedulesEvent.PostFailed(rent))
    }
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
  fun `setShowCompleted persists the choice`() = runDatabaseTest { scope ->
    val preferences = SchedulePreferencesImpl(scope.buildPreferences())
    val viewModel = createViewModel(scope, TestSyncController(), preferences)

    viewModel.showCompleted.test {
      assertThat(awaitItem()).isFalse()

      viewModel.setShowCompleted(true)
      assertThat(awaitItem()).isTrue()
      assertThat(preferences.showCompleted.get()).isTrue()

      cancelAndIgnoreRemainingEvents()
    }
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
        calendar = TODAY,
      )
    val writer =
      ScheduleWriter(
        scheduleDao = ScheduleDao(this),
        syncController = sync,
        uuidGenerator = UuidGenerator { error("unused") },
        calendar = TODAY,
        clock = TestClock(NOW),
      )
    var nextId = 1
    val uuidGenerator = UuidGenerator { "id-${nextId++}" }
    val poster =
      ScheduleTransactionPoster(
        scheduleDao = ScheduleDao(this),
        transactionWriter =
          TransactionWriter(
            syncController = sync,
            accountDao = AccountDao(this),
            categoryDao = CategoryDao(this),
            payeeDao = PayeeDao(this),
            transactionDao = TransactionDao(this),
            categoryLearner = {},
            uuidGenerator = uuidGenerator,
            clock = TestClock(NOW),
          ),
        rulesLoader =
          TransactionRulesLoader(
            rulesDao = RulesDao(this),
            contextDao = RuleContextDao(this),
            uuidGenerator = uuidGenerator,
          ),
        uuidGenerator = uuidGenerator,
      )
    return ListSchedulesViewModel(
      loader = loader,
      syncController = sync,
      writer = writer,
      poster = poster,
      preferences = preferences,
      calendar = TODAY,
      viewModelContext = scope.viewModelContext,
    )
  }

  private fun TestSyncController.posted(column: String): List<MessageValue> =
    changes.filter { it.dataset == TRANSACTIONS && it.column == column }.map { it.value }

  private companion object {
    val TODAY = Calendar { LocalDate(2026, 4, 1) }
    val NOW = Instant.parse("2026-04-01T12:00:00Z")
  }

  private object FailingSyncController : BudgetSyncController {
    override suspend fun syncChanges(changes: List<LocalChange>): Unit = error("sync failed")

    override fun schedule() = Unit
  }
}
