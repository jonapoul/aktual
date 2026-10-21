package aktual.budget.schedules.domain

import aktual.budget.BudgetSyncController
import aktual.budget.SyncEvents
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Rules
import aktual.budget.db.Transactions
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.DatabaseTables.PAYEES
import aktual.budget.db.dao.DatabaseTables.SCHEDULES
import aktual.budget.db.dao.DatabaseTables.SCHEDULES_NEXT_DATE
import aktual.budget.db.dao.DatabaseTables.TRANSACTIONS
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.Condition
import aktual.budget.model.DbMetadata.Companion.LastScheduleRun
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.model.TransactionId
import aktual.budget.rules.domain.TransactionRulesLoader
import aktual.budget.transactions.domain.TransactionWriter
import aktual.core.UuidGenerator
import aktual.di.BudgetCoroutineScope
import aktual.test.TestBudgetLocalPreferences
import aktual.test.TestCalendar
import aktual.test.TestSyncController
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.insertSchedule
import aktual.test.runDatabaseTest
import alakazam.test.TestClock
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.datetime.LocalDate
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

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
  fun `Missed schedule that posts adds its transaction through the rules`() =
    runDatabaseTest { scope ->
      insertSchedule(id = "a", nextDate = MARCH_15, postsTransaction = true)
      insertRule(
        payee = "a-payee",
        actions = """[{"op": "set", "field": "notes", "value": "Rent"}]""",
      )

      advancer(scope).onSyncCompleted(Succeeded)

      assertThat(posted("acct")).containsExactly(MessageValue.String("a-account"))
      assertThat(posted("date")).containsExactly(MessageValue.Number(20260315))
      assertThat(posted("amount")).containsExactly(MessageValue.Number(-1000))
      assertThat(posted("description")).containsExactly(MessageValue.String("a-payee"))
      assertThat(posted("schedule")).containsExactly(MessageValue.String("a"))
      assertThat(posted("cleared")).containsExactly(MessageValue.Number(0))
      assertThat(posted("notes")).containsExactly(MessageValue.String("Rent"))
      assertThat(syncController.changes.filter { it.dataset == SCHEDULES }).isEmpty()
    }

  @Test
  fun `Missed recurring schedule posts each date until it's caught up`() =
    runDatabaseTest { scope ->
      insertSchedule(
        id = "a",
        nextDate = FEB_15,
        date = MONTHLY_FROM_FEB_15,
        postsTransaction = true,
      )

      advancer(scope).onSyncCompleted(Succeeded)

      assertThat(posted("date"))
        .containsExactly(MessageValue.Number(20260215), MessageValue.Number(20260315))
      assertThat(nextDateChanges()).containsExactly(MessageValue.Number(20260415))
    }

  @Test
  fun `Schedule due today posts once and keeps its date`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = TODAY, date = MONTHLY_FROM_TODAY, postsTransaction = true)

    advancer(scope).onSyncCompleted(Succeeded)

    assertThat(posted("date")).containsExactly(MessageValue.Number(20260401))
    assertThat(nextDateChanges()).isEmpty()
  }

  @Test
  fun `Paid recurring schedule that posts moves on without posting again`() =
    runDatabaseTest { scope ->
      insertSchedule(
        id = "a",
        nextDate = MARCH_15,
        date = MONTHLY_FROM_MARCH_15,
        postsTransaction = true,
      )
      insertTransaction(schedule = "a", date = MARCH_15)

      advancer(scope).onSyncCompleted(Succeeded)

      assertThat(posted("date")).isEmpty()
      assertThat(nextDateChanges()).containsExactly(MessageValue.Number(20260415))
    }

  @Test
  fun `Failed sync doesn't post`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15, postsTransaction = true)

    advancer(scope).onSyncCompleted(Failed)

    assertThat(posted("date")).isEmpty()
  }

  @Test
  fun `Rule that deletes the transaction stops it posting`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15, postsTransaction = true)
    insertRule(payee = "a-payee", actions = """[{"op": "delete-transaction", "value": ""}]""")

    advancer(scope).onSyncCompleted(Succeeded)

    assertThat(posted("date")).isEmpty()
  }

  @Test
  fun `Payee a rule creates is added with the transaction`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15, postsTransaction = true)
    insertRule(
      payee = "a-payee",
      actions = """[{"op": "set", "field": "payee_name", "value": "Landlord"}]""",
    )

    advancer(scope).onSyncCompleted(Succeeded)

    val payee = syncController.changes.single { it.dataset == PAYEES && it.column == "name" }
    assertThat(payee.value).isEqualTo(MessageValue.String("Landlord"))
    assertThat(posted("description")).containsExactly(MessageValue.String(payee.row))
  }

  @Test
  fun `Rule that splits the transaction posts its parts`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15, postsTransaction = true)
    insertRule(
      payee = "a-payee",
      actions =
        """
        [
          {"op": "set-split-amount", "value": 60, "options": {"splitIndex": 1, "method": "fixed-percent"}},
          {"op": "set-split-amount", "value": null, "options": {"splitIndex": 2, "method": "remainder"}}
        ]
        """
          .trimIndent(),
    )

    advancer(scope).onSyncCompleted(Succeeded)

    assertThat(posted("amount"))
      .containsExactly(
        MessageValue.Number(-1000),
        MessageValue.Number(-600),
        MessageValue.Number(-400),
      )
    assertThat(posted("isParent")).containsExactly(MessageValue.Number(1))
    assertThat(posted("parent_id")).hasSize(2)
  }

  @Test
  fun `Failed posting doesn't count as today's run`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15, postsTransaction = true)
    val failingSync =
      object : BudgetSyncController {
        override suspend fun syncChanges(changes: List<LocalChange>) = error("Sync failed")

        override fun schedule() = Unit
      }

    advancer(scope, sync = failingSync).onSyncCompleted(Succeeded)

    assertThat(budgetPrefs[LastScheduleRun]).isNull()
  }

  @Test
  fun `Sync our own changes complete doesn't post again`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15, postsTransaction = true)
    // Only completes once, so posting twice fails the test rather than looping
    var completed = false
    val completingSync =
      object : BudgetSyncController {
        override suspend fun syncChanges(changes: List<LocalChange>) {
          syncController.syncChanges(changes)
          if (!completed) {
            completed = true
            syncEvents.complete(Succeeded)
          }
        }

        override fun schedule() = Unit
      }
    advancer(scope, BudgetCoroutineScope(scope.backgroundScope), sync = completingSync).initialize()

    syncEvents.complete(Succeeded)
    scope.runCurrent()

    assertThat(posted("date")).containsExactly(MessageValue.Number(20260315))
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

  @Test
  fun `Failed sync with schedules to post flags them as unposted`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15, postsTransaction = true)
    val advancer = advancer(scope)

    advancer.hasUnposted.test {
      assertThat(awaitItem()).isFalse()
      advancer.onSyncCompleted(Failed)
      assertThatNextEmissionIsEqualTo(true)
    }
  }

  @Test
  fun `Failed sync with nothing to post doesn't flag anything`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15)
    val advancer = advancer(scope)

    advancer.hasUnposted.test {
      assertThat(awaitItem()).isFalse()
      advancer.onSyncCompleted(Failed)
      expectNoEvents()
    }
  }

  @Test
  fun `Posting unposted schedules posts them despite the failed sync`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15, postsTransaction = true)
    val advancer = advancer(scope)

    advancer.hasUnposted.test {
      assertThat(awaitItem()).isFalse()
      advancer.onSyncCompleted(Failed)
      assertThatNextEmissionIsEqualTo(true)

      advancer.post()
      assertThatNextEmissionIsEqualTo(false)
      scope.runCurrent()
      assertThat(posted("date")).containsExactly(MessageValue.Number(20260315))
    }
  }

  @Test
  fun `Dismissing unposted schedules stops flagging them`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", nextDate = MARCH_15, postsTransaction = true)
    val advancer = advancer(scope)

    advancer.hasUnposted.test {
      assertThat(awaitItem()).isFalse()
      advancer.onSyncCompleted(Failed)
      assertThatNextEmissionIsEqualTo(true)

      advancer.dismiss()
      assertThatNextEmissionIsEqualTo(false)
      advancer.onSyncCompleted(Failed)
      expectNoEvents()
    }
    assertThat(posted("date")).isEmpty()
  }

  private suspend fun BudgetDatabase.insertSchedule(
    id: String,
    nextDate: LocalDate,
    date: String = "\"$nextDate\"",
    postsTransaction: Boolean = false,
  ) =
    insertSchedule(
      id = id,
      name = id,
      payee = "Payee",
      account = "Account",
      nextDate = nextDate,
      date = date,
      postsTransaction = postsTransaction,
    )

  private suspend fun BudgetDatabase.insertRule(payee: String, actions: String) =
    RulesDao(this)
      .insert(
        Rules(
          id = RuleId("rule"),
          stage = null,
          conditions =
            Json.decodeFromString(
              ListSerializer(Condition.serializer()),
              """[{"op": "is", "field": "description", "value": "$payee"}]""",
            ),
          actions = Json.decodeFromString(ListSerializer(RuleAction.serializer()), actions),
          tombstone = false,
          conditions_op = null,
        ),
      )

  private fun posted(column: String): List<MessageValue> =
    syncController.changes
      .filter { it.dataset == TRANSACTIONS && it.column == column }
      .map { it.value }

  private fun nextDateChanges(): List<MessageValue> =
    syncController.changes
      .filter { it.dataset == SCHEDULES_NEXT_DATE && it.column == "local_next_date" }
      .map { it.value }

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
    sync: BudgetSyncController = syncController,
  ): ScheduleAdvancer {
    var nextId = 1
    val uuidGenerator = UuidGenerator { "id-${nextId++}" }
    return ScheduleAdvancer(
      scheduleDao = ScheduleDao(this),
      accountDao = AccountDao(this),
      preferencesDao =
        PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))),
      syncController = sync,
      syncEvents = syncEvents,
      budgetPrefs = budgetPrefs,
      poster =
        SchedulePoster(
          rulesLoader =
            TransactionRulesLoader(
              rulesDao = RulesDao(this),
              contextDao = RuleContextDao(this),
              uuidGenerator = uuidGenerator,
            ),
          transactionWriter =
            TransactionWriter(
              syncController = sync,
              accountDao = AccountDao(this),
              categoryDao = CategoryDao(this),
              payeeDao = PayeeDao(this),
              transactionDao = TransactionDao(this),
              uuidGenerator = uuidGenerator,
              clock = TestClock(Instant.fromEpochMilliseconds(0)),
            ),
          uuidGenerator = uuidGenerator,
        ),
      scope = coroutineScope,
      calendar = calendar,
    )
  }

  private companion object {
    val TODAY = LocalDate(2026, 4, 1)
    val MARCH_15 = LocalDate(2026, 3, 15)
    val FEB_15 = LocalDate(2026, 2, 15)
    const val MONTHLY_FROM_MARCH_15 =
      """{"start": "2026-03-15", "frequency": "monthly", "interval": 1, "endMode": "never"}"""
    const val MONTHLY_FROM_FEB_15 =
      """{"start": "2026-02-15", "frequency": "monthly", "interval": 1, "endMode": "never"}"""
    const val MONTHLY_FROM_TODAY =
      """{"start": "2026-04-01", "frequency": "monthly", "interval": 1, "endMode": "never"}"""
  }
}
