package aktual.budget.payees.vm.list

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Rules
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.Condition
import aktual.budget.model.MessageValue
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId
import aktual.budget.payees.vm.PayeesLoader
import aktual.budget.rules.domain.PayeeRuleCounter
import aktual.di.BudgetCoroutineScope
import aktual.test.TestSyncController
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import alakazam.test.standardDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import kotlin.test.AfterTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ListPayeesViewModelTest {
  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

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
              numTransactions = 0,
              numRules = 0,
            ),
            PayeeItem(
              id = PayeeId("transfer"),
              name = "Current",
              isTransfer = true,
              isFavorite = false,
              numTransactions = 0,
              numRules = 1,
            ),
            PayeeItem(
              id = PayeeId("tesco"),
              name = "Tesco",
              isTransfer = false,
              isFavorite = false,
              numTransactions = 2,
              numRules = 1,
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
  fun `Selects and deselects payees, but not transfers`() = runDatabaseTest { scope ->
    // given
    insertPayees()
    val viewModel = createViewModel(scope).awaitLoaded()

    viewModel.selected.test {
      assertThatNextEmissionIsEqualTo(persistentSetOf())

      // when
      viewModel.toggleSelected(ARGOS.id)
      assertThatNextEmissionIsEqualTo(persistentSetOf(ARGOS.id))

      viewModel.toggleSelected(TRANSFER)
      viewModel.toggleSelected(ARGOS.id)
      assertThatNextEmissionIsEqualTo(persistentSetOf())
    }
  }

  @Test
  fun `Select all leaves out transfers`() = runDatabaseTest { scope ->
    // given
    insertPayees()
    val viewModel = createViewModel(scope).awaitLoaded()

    viewModel.selected.test {
      assertThatNextEmissionIsEqualTo(persistentSetOf())

      // when
      viewModel.selectAll()
      assertThatNextEmissionIsEqualTo(persistentSetOf(ARGOS.id, TESCO))

      viewModel.clearSelection()
      assertThatNextEmissionIsEqualTo(persistentSetOf())
    }
  }

  @Test
  fun `Deleting tombstones the selected payees and undo restores them`() =
    runDatabaseTest { scope ->
      // given
      insertPayees()
      val sync = TestSyncController()
      val viewModel = createViewModel(scope, sync).awaitLoaded()
      viewModel.selectAll()

      // when
      viewModel.events.test {
        viewModel.deleteSelected()
        assertThatNextEmissionIsEqualTo(ListPayeesEvent.Deleted(persistentSetOf(ARGOS.id, TESCO)))
      }

      // then
      assertThat(viewModel.selected.value).isEmpty()
      assertThat(sync.changes.map { it.row to it.column to it.value })
        .containsExactlyInAnyOrder(
          "argos" to "tombstone" to MessageValue.Number(1),
          "tesco" to "tombstone" to MessageValue.Number(1),
        )

      // when
      sync.changes.clear()
      viewModel.undoDelete(setOf(ARGOS.id, TESCO))
      scope.advanceUntilIdle()

      // then
      assertThat(sync.changes.map { it.row to it.column to it.value })
        .containsExactlyInAnyOrder(
          "argos" to "tombstone" to MessageValue.Number(0),
          "tesco" to "tombstone" to MessageValue.Number(0),
        )
    }

  @Test
  fun `Favourites all the selected payees unless they all are already`() =
    runDatabaseTest { scope ->
      // given
      insertPayees()
      payeesQueries.withoutResult {
        setNameAndFavorite(name = "Tesco", favorite = true, id = TESCO)
      }
      val sync = TestSyncController()
      val viewModel = createViewModel(scope, sync).awaitLoaded()

      // when some are favourites
      viewModel.selectAll()
      viewModel.toggleFavoriteSelected()
      scope.advanceUntilIdle()

      // then
      assertThat(sync.changes.map { it.row to it.value })
        .containsExactlyInAnyOrder(
          "argos" to MessageValue.Number(1),
          "tesco" to MessageValue.Number(1),
        )
      assertThat(viewModel.selected.value).isEmpty()

      // when they all are
      sync.changes.clear()
      viewModel.toggleSelected(TESCO)
      viewModel.toggleFavoriteSelected()
      scope.advanceUntilIdle()

      // then
      assertThat(sync.changes.map { it.row to it.value })
        .containsExactly("tesco" to MessageValue.Number(0))
    }

  @Test
  fun `Payees deleted elsewhere drop out of the selection`() = runDatabaseTest { scope ->
    // given
    insertPayees()
    val viewModel = createViewModel(scope).awaitLoaded()
    viewModel.selectAll()

    viewModel.selected.test {
      assertThatNextEmissionIsEqualTo(persistentSetOf(ARGOS.id, TESCO))

      // when
      payeesQueries.withoutResult {
        insert(TESCO, "Tesco", null, true, null, false, null)
      }

      // then
      assertThatNextEmissionIsEqualTo(persistentSetOf(ARGOS.id))
    }
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
    Dispatchers.setMain(scope.standardDispatcher)
    return ListPayeesViewModel(
      loader = PayeesLoader(PayeeDao(this), PayeeRuleCounter(RulesDao(this), RuleContextDao(this))),
      syncController = sync,
      budgetScope = BudgetCoroutineScope(scope),
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
        numTransactions = 0,
        numRules = 0,
      )
  }
}
