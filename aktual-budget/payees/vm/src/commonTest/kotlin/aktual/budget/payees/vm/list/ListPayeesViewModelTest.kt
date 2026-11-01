package aktual.budget.payees.vm.list

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Rules
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.Condition
import aktual.budget.model.MessageValue
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId
import aktual.budget.model.SyncedPrefKey.Global.LearnCategories
import aktual.budget.payees.domain.PayeeMerge
import aktual.budget.payees.domain.PayeeMerger
import aktual.budget.payees.vm.PayeesLoader
import aktual.budget.rules.domain.CategoryLearningPref
import aktual.budget.rules.domain.PayeeRuleCounter
import aktual.di.BudgetCoroutineScope
import aktual.test.TestSyncController
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import aktual.test.viewModelContext
import alakazam.test.TestCoroutineContexts
import alakazam.test.standardDispatcher
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class ListPayeesViewModelTest {
  @Test
  fun `Loads payees with their transaction and rule counts`() = runDatabaseTest { scope ->
    // given
    insertAccount(ACCOUNT, name = "Current")
    val payees = PayeeDao(this)
    payees.insert(PayeeId("argos"), "Argos")
    payees.insert(PayeeId("tesco"), "Tesco")
    payees.insert(PayeeId("transfer"), null, transferAccount = AccountId(ACCOUNT))
    val transactions = TransactionDao(this)
    transactions.insert("t1", ACCOUNT, null, "tesco", DATE)
    transactions.insert("t2", ACCOUNT, null, "tesco", DATE)
    insertPayeeRule("r1", payee = "tesco")
    insertPayeeRule("r2", payee = "transfer")

    // when
    val viewModel = createViewModel(scope)

    // then
    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(Loading)
      assertThatNextEmissionIsEqualTo(
        Success(
          persistentListOf(
            PayeeItem(
              id = PayeeId("argos"),
              name = "Argos",
              isTransfer = false,
              isFavorite = false,
              learnsCategories = true,
              numTransactions = 0,
              numRules = 0,
              isUnused = true,
            ),
            PayeeItem(
              id = PayeeId("transfer"),
              name = "Current",
              isTransfer = true,
              isFavorite = false,
              learnsCategories = true,
              numTransactions = 0,
              numRules = 1,
              isUnused = false,
            ),
            PayeeItem(
              id = PayeeId("tesco"),
              name = "Tesco",
              isTransfer = false,
              isFavorite = false,
              learnsCategories = true,
              numTransactions = 2,
              numRules = 1,
              isUnused = false,
            ),
          ),
        ),
      )
    }
  }

  @Test
  fun `No payees gives the empty state`() = runDatabaseTest { scope ->
    createViewModel(scope).state.test {
      assertThatNextEmissionIsEqualTo(Loading)
      assertThatNextEmissionIsEqualTo(Empty)
    }
  }

  @Test
  fun `Updates when a payee is added`() = runDatabaseTest { scope ->
    // given
    val viewModel = createViewModel(scope)
    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(Loading)
      assertThatNextEmissionIsEqualTo(Empty)

      // when
      PayeeDao(this@runDatabaseTest).insert(PayeeId("argos"), "Argos")

      // then
      assertThatNextEmissionIsEqualTo(Success(persistentListOf(ARGOS)))
    }
  }

  @Test
  fun `Updates rule counts when a rule is added`() = runDatabaseTest { scope ->
    // given
    PayeeDao(this).insert(PayeeId("argos"), "Argos")
    val viewModel = createViewModel(scope)
    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(Loading)
      assertThatNextEmissionIsEqualTo(Success(persistentListOf(ARGOS)))

      // when
      insertPayeeRule("r1", payee = "argos")

      // then
      assertThatNextEmissionIsEqualTo(Success(persistentListOf(ARGOS.copy(numRules = 1))))
    }
  }

  @Test
  fun `The unused filter shows only payees without transactions`() = runDatabaseTest { scope ->
    // given
    insertPayees()
    TransactionDao(this).insert("t1", ACCOUNT, null, TESCO.value, DATE)
    val viewModel = createViewModel(scope)
    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(Loading)
      val all = (awaitItem() as Success).payees
      assertThat(all.filter { it.isUnused }.map { it.id }).containsExactly(ARGOS.id)

      // when
      viewModel.toggleUnusedOnly()

      // then
      val filtered = awaitItem() as Success
      assertThat(filtered).all {
        prop(Success::payees).isEqualTo(all)
        prop(Success::numUnused).isEqualTo(1)
        prop(Success::shown).containsExactly(ARGOS)
      }

      // when
      viewModel.toggleUnusedOnly()

      // then
      assertThatNextEmissionIsEqualTo(Success(all, unusedOnly = false))
    }
  }

  @Test
  fun `Deleting tombstones the payee and undo restores it`() = runDatabaseTest { scope ->
    // given
    insertPayees()
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, sync).awaitLoaded()

    // when
    viewModel.events.test {
      viewModel.delete(ARGOS)
      assertThatNextEmissionIsEqualTo(ListPayeesEvent.Deleted(ARGOS))
    }

    // then
    assertThat(sync.changes.map { it.row to it.column to it.value })
      .containsExactly("argos" to "tombstone" to MessageValue.Number(1))

    // when
    sync.changes.clear()
    viewModel.undoDelete(ARGOS)
    scope.advanceUntilIdle()

    // then
    assertThat(sync.changes.map { it.row to it.column to it.value })
      .containsExactly("argos" to "tombstone" to MessageValue.Number(0))
  }

  @Test
  fun `Toggling favourite flips the payee's flag`() = runDatabaseTest { scope ->
    // given
    insertPayees()
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, sync).awaitLoaded()

    // when
    viewModel.toggleFavorite(ARGOS)
    viewModel.toggleFavorite(ARGOS.copy(isFavorite = true))
    scope.advanceUntilIdle()

    // then
    assertThat(sync.changes.map { it.row to it.column to it.value })
      .containsExactly(
        "argos" to "favorite" to MessageValue.Number(1),
        "argos" to "favorite" to MessageValue.Number(0),
      )
  }

  @Test
  fun `Merging moves the payee into the target and undo restores it`() = runDatabaseTest { scope ->
    // given
    insertPayees()
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, sync).awaitLoaded()
    val tesco = ARGOS.copy(id = TESCO, name = "Tesco")
    val merged =
      ListPayeesEvent.Merged(
        payee = tesco,
        target = ARGOS,
        merge =
          PayeeMerge(target = ARGOS.id, merged = listOf(TESCO), oldTargets = mapOf(TESCO to TESCO)),
      )

    // when
    viewModel.events.test {
      viewModel.merge(tesco, into = ARGOS)
      assertThatNextEmissionIsEqualTo(merged)
    }

    // then
    assertThat(sync.changes.map { it.row to it.column to it.value })
      .containsExactly(
        "tesco" to "targetId" to MessageValue.String("argos"),
        "tesco" to "tombstone" to MessageValue.Number(1),
      )

    // when
    sync.changes.clear()
    viewModel.undoMerge(merged)
    scope.advanceUntilIdle()

    // then
    assertThat(sync.changes.map { it.row to it.column to it.value })
      .containsExactly(
        "tesco" to "tombstone" to MessageValue.Number(0),
        "tesco" to "targetId" to MessageValue.String("tesco"),
      )
  }

  @Test
  fun `A merge with nothing to merge reports a failure`() = runDatabaseTest { scope ->
    // given
    insertPayees()
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, sync).awaitLoaded()
    val transfer = ARGOS.copy(id = TRANSFER, isTransfer = true)

    // when
    viewModel.events.test {
      viewModel.merge(transfer, into = ARGOS)

      // then
      assertThatNextEmissionIsEqualTo(MergeFailed)
    }
    assertThat(sync.changes).isEmpty()
  }

  @Test
  fun `Category learning follows the synced pref`() = runDatabaseTest { scope ->
    // given
    insertPayees()
    val viewModel = createViewModel(scope)
    val preferences = PreferencesDao(this, TestCoroutineContexts(scope.standardDispatcher))

    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(Loading)
      assertThat((awaitItem() as Success).isLearningEnabled).isTrue()

      // when
      preferences[LearnCategories] = "false"

      // then
      assertThat((awaitItem() as Success).isLearningEnabled).isFalse()
    }
  }

  @Test
  fun `Setting category learning syncs the pref`() = runDatabaseTest { scope ->
    // given
    insertPayees()
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, sync).awaitLoaded()

    // when
    viewModel.setLearningEnabled(false)
    scope.advanceUntilIdle()

    // then
    assertThat(sync.changes.map { it.row to it.column to it.value })
      .containsExactly("learn-categories" to "value" to MessageValue.String("false"))
  }

  @Test
  fun `Transfer payees can't be deleted or favourited`() = runDatabaseTest { scope ->
    // given
    insertPayees()
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, sync).awaitLoaded()
    val transfer = ARGOS.copy(id = TRANSFER, isTransfer = true)

    // when
    viewModel.delete(transfer)
    viewModel.toggleFavorite(transfer)
    scope.advanceUntilIdle()

    // then
    assertThat(sync.changes).isEmpty()
  }

  private suspend fun ListPayeesViewModel.awaitLoaded(): ListPayeesViewModel = apply {
    state.test {
      do {
        val item = awaitItem()
      } while (item !is Success)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private suspend fun BudgetDatabase.insertPayees() {
    insertAccount(ACCOUNT, name = "Current")
    val payees = PayeeDao(this)
    payees.insert(ARGOS.id, "Argos")
    payees.insert(TESCO, "Tesco")
    payees.insert(TRANSFER, null, transferAccount = AccountId(ACCOUNT))
  }

  private fun BudgetDatabase.createViewModel(
    scope: TestScope,
    sync: TestSyncController = TestSyncController(),
  ): ListPayeesViewModel {
    val preferences = PreferencesDao(this, TestCoroutineContexts(scope.standardDispatcher))
    return ListPayeesViewModel(
      loader = PayeesLoader(PayeeDao(this), PayeeRuleCounter(RulesDao(this), RuleContextDao(this))),
      syncController = sync,
      merger = PayeeMerger(PayeeDao(this), sync),
      learning = CategoryLearningPref(preferences, sync),
      budgetScope = BudgetCoroutineScope(scope),
      viewModelContext = scope.viewModelContext,
    )
  }

  private suspend fun BudgetDatabase.insertAccount(id: String, name: String) =
    accountsQueries.withoutResult {
      insert(
        id = AccountId(id),
        account_id = null,
        name = name,
        official_name = null,
        bank = null,
        offbudget = false,
        account_sync_source = null,
      )
    }

  private suspend fun BudgetDatabase.insertPayeeRule(id: String, payee: String) =
    RulesDao(this)
      .insert(
        Rules(
          id = RuleId(id),
          stage = null,
          conditions =
            listOf(Condition(field = Payee, operator = Is, value = JsonPrimitive(payee))),
          actions = emptyList(),
          tombstone = false,
          conditions_op = And,
        ),
      )

  private companion object {
    const val ACCOUNT = "account"
    val TESCO = PayeeId("tesco")
    val TRANSFER = PayeeId("transfer")
    val DATE = LocalDate(2026, 10, 1)

    val ARGOS =
      PayeeItem(
        id = PayeeId("argos"),
        name = "Argos",
        isTransfer = false,
        isFavorite = false,
        learnsCategories = true,
        numTransactions = 0,
        numRules = 0,
        isUnused = true,
      )
  }
}
