package aktual.budget.schedules.domain

import aktual.budget.SyncEvents
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Transactions
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.DatabaseTables.SCHEDULES
import aktual.budget.db.dao.DatabaseTables.SCHEDULES_NEXT_DATE
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.DbMetadata.Companion.LastScheduleRun
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.ScheduleId
import aktual.budget.model.TransactionId
import aktual.di.BudgetCoroutineScope
import aktual.test.TestBudgetLocalPreferences
import aktual.test.TestCalendar
import aktual.test.TestSyncController
import aktual.test.insertSchedule
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.datetime.LocalDate

class ScheduleAdvancerTest {
  private val syncController = TestSyncController()
  private val syncEvents = SyncEvents()
  private val budgetPrefs = TestBudgetLocalPreferences()
  private val calendar = TestCalendar(TODAY)

  @Test
  fun `Paid recurring schedule moves to its next date from today`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15, date = MONTHLY_FROM_MARCH_15)
    insertTransaction(schedule = "a", date = MARCH_15)

    advancer(scope).onSyncCompleted(Succeeded)

    assertThat(syncController.changes)
      .containsExactly(
        LocalChange(
          SCHEDULES_NEXT_DATE,
          "a-next",
          "local_next_date",
          MessageValue.Number(20260415),
        ),
        LocalChange(SCHEDULES_NEXT_DATE, "a-next", "local_next_date_ts", MessageValue.Number(0)),
      )
  }

  @Test
  fun `Paid one-off schedule in the past is completed`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15)
    insertTransaction(schedule = "a", date = MARCH_15)

    advancer(scope).onSyncCompleted(Succeeded)

    assertThat(syncController.changes)
      .containsExactly(LocalChange(SCHEDULES, "a", "completed", MessageValue.Number(1)))
  }

  @Test
  fun `Paid one-off schedule due today stays open`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = TODAY)
    insertTransaction(schedule = "a", date = TODAY)

    advancer(scope).onSyncCompleted(Succeeded)

    assertThat(syncController.changes).isEmpty()
  }

  @Test
  fun `Unpaid schedules are left alone`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15, date = MONTHLY_FROM_MARCH_15)
    insertSchedule(id = "b", nextDate = MARCH_15)

    advancer(scope).onSyncCompleted(Succeeded)

    assertThat(syncController.changes).isEmpty()
  }

  @Test
  fun `Schedules on closed accounts are skipped`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15)
    insertTransaction(schedule = "a", date = MARCH_15)
    accountsQueries.withoutResult { closeAccount(AccountId("a-account")) }

    advancer(scope).onSyncCompleted(Succeeded)

    assertThat(syncController.changes).isEmpty()
  }

  @Test
  fun `Runs once a day after a successful sync`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15)
    insertTransaction(schedule = "a", date = MARCH_15)
    val advancer = advancer(scope)

    advancer.onSyncCompleted(Succeeded)
    assertThat(budgetPrefs[LastScheduleRun]).isEqualTo(TODAY)
    syncController.changes.clear()

    advancer.onSyncCompleted(Succeeded)
    assertThat(syncController.changes).isEmpty()
  }

  @Test
  fun `Failed sync still advances but doesn't count as today's run`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15)
    insertTransaction(schedule = "a", date = MARCH_15)

    advancer(scope).onSyncCompleted(Failed)

    assertThat(syncController.changes)
      .containsExactly(LocalChange(SCHEDULES, "a", "completed", MessageValue.Number(1)))
    assertThat(budgetPrefs[LastScheduleRun]).isNull()
  }

  @Test
  fun `Runs when a sync completes`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15)
    insertTransaction(schedule = "a", date = MARCH_15)
    advancer(scope, BudgetCoroutineScope(scope.backgroundScope)).initialize()

    syncEvents.complete(Succeeded)
    scope.runCurrent()

    assertThat(syncController.changes)
      .containsExactly(LocalChange(SCHEDULES, "a", "completed", MessageValue.Number(1)))
  }

  private suspend fun BudgetDatabase.insertSchedule(
    id: String,
    nextDate: LocalDate,
    date: String = "\"$nextDate\"",
  ) =
    insertSchedule(
      id = id,
      name = id,
      payee = "Payee",
      account = "Account",
      nextDate = nextDate,
      date = date,
    )

  private suspend fun BudgetDatabase.insertTransaction(schedule: String, date: LocalDate) =
    transactionsQueries.withoutResult {
      insert(
        Transactions(
          id = TransactionId("$schedule-tx"),
          isParent = false,
          isChild = false,
          acct = AccountId("$schedule-account"),
          category = null,
          amount = null,
          description = null,
          notes = null,
          date = date,
          financial_id = null,
          type = null,
          location = null,
          error = null,
          imported_description = null,
          starting_balance_flag = false,
          transferred_id = null,
          sort_order = null,
          tombstone = false,
          cleared = true,
          pending = false,
          parent_id = null,
          schedule = ScheduleId(schedule),
          reconciled = false,
          raw_synced_data = null,
        ),
      )
    }

  private fun BudgetDatabase.advancer(
    scope: TestScope,
    coroutineScope: BudgetCoroutineScope = BudgetCoroutineScope(scope),
  ) =
    ScheduleAdvancer(
      scheduleDao = ScheduleDao(this),
      accountDao = AccountDao(this),
      preferencesDao =
        PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))),
      syncController = syncController,
      syncEvents = syncEvents,
      budgetPrefs = budgetPrefs,
      scope = coroutineScope,
      calendar = calendar,
    )

  private companion object {
    val TODAY = LocalDate(2026, 4, 1)
    val MARCH_15 = LocalDate(2026, 3, 15)
    const val MONTHLY_FROM_MARCH_15 =
      """{"start": "2026-03-15", "frequency": "monthly", "interval": 1, "endMode": "never"}"""
  }
}
