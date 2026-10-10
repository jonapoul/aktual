package aktual.budget.transactions.vm.edit

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.DatabaseTables.TRANSACTIONS
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.budget.model.tombstone
import aktual.budget.transactions.domain.TransactionFields
import aktual.budget.transactions.domain.TransactionLoader
import aktual.budget.transactions.domain.TransactionWriter
import aktual.budget.transactions.vm.DATE_1
import aktual.budget.transactions.vm.asParent
import aktual.budget.transactions.vm.insertAccount
import aktual.budget.transactions.vm.insertCategory
import aktual.budget.transactions.vm.insertPayee
import aktual.budget.transactions.vm.insertTransaction
import aktual.budget.transactions.vm.transaction
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import app.cash.turbine.Event
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.each
import assertk.assertions.extracting
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.AfterTest
import kotlin.time.Clock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EditTransactionViewModelTest {
  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `A transaction opens in view mode`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val viewModel = createViewModel(scope, id = "t")

    viewModel.state.test {
      assertThat(awaitLoaded())
        .isEqualTo(
          EditTransactionState.Loaded(
            saved =
              TransactionDetails(
                transaction =
                  transaction("t", account = "a", category = "a", payee = "a", balance = 123.45),
                cleared = true,
                reconciled = false,
              ),
            mode = View,
            canEdit = true,
          ),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A split part opens its whole split`() = runDatabaseTest { scope ->
    insertEntities()
    with(TransactionDao(this)) {
      insertTransaction("p", "a", null, "a", amount = 100.0, isParent = true)
      insertTransaction("p1", "a", "b", "b", amount = 60.0, parent = "p")
      insertTransaction("p2", "a", "c", "b", amount = 40.0, parent = "p")
    }
    val viewModel = createViewModel(scope, id = "p2")

    viewModel.state.test {
      assertThat(awaitLoaded().saved.transaction)
        .isEqualTo(
          transaction("p", "a", null, "a", amount = 100.0, balance = 100.0)
            .asParent(
              payee = "B&Q",
              transaction("p1", "a", "b", "b", amount = 60.0),
              transaction("p2", "a", "c", "b", amount = 40.0),
            ),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Changes made elsewhere are followed`() = runDatabaseTest { scope ->
    insertEntities()
    val transactions = TransactionDao(this)
    transactions.insertTransaction("p", "a", null, "a", amount = 100.0, isParent = true)
    transactions.insertTransaction("p1", "a", "b", "b", amount = 60.0, parent = "p")
    val viewModel = createViewModel(scope, id = "p")

    viewModel.state.test {
      assertThat(awaitLoaded().saved.transaction.children.size).isEqualTo(1)

      transactions.insertTransaction("p2", "a", "c", "b", amount = 40.0, parent = "p")
      scope.advanceUntilIdle()

      assertThat(awaitLoaded().saved.transaction.children.size).isEqualTo(2)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A missing transaction isn't found`() = runDatabaseTest { scope ->
    insertEntities()
    val viewModel = createViewModel(scope, id = "missing")

    viewModel.state.test {
      var state = awaitItem()
      while (state == Loading) state = awaitItem()
      assertThat(state).isEqualTo(EditTransactionState.Failure.NotFound)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Editing starts from what's saved`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val viewModel = createViewModel(scope, id = "t")

    viewModel.state.test {
      awaitLoaded()
      viewModel.startEditing()

      assertThat(awaitEdit()).all {
        prop(TransactionEditMode.Edit::draft).isEqualTo(SAVED)
        prop(TransactionEditMode.Edit::payeeName).isEqualTo("Argos")
        prop(TransactionEditMode.Edit::categoryName).isEqualTo("Additional")
        prop(TransactionEditMode.Edit::accountName).isEqualTo("Amex")
        prop(TransactionEditMode.Edit::isOffBudget).isFalse()
        prop(TransactionEditMode.Edit::hasChanges).isFalse()
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `The pickers offer open accounts and ordinary payees`() = runDatabaseTest { scope ->
    insertEntities()
    AccountDao(this).insert(id = AccountId("d"), name = "Mortgage", offBudget = true)
    PayeeDao(this).insert(PayeeId("transfer"), name = null, transferAccount = AccountId("b"))
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val viewModel = createViewModel(scope, id = "t")

    viewModel.state.test {
      awaitLoaded()
      viewModel.startEditing()

      assertThat(awaitEdit().options).all {
        prop(TransactionOptions::payees)
          .extracting { it.name }
          .containsExactly("Argos", "B&Q", "Co-op")
        prop(TransactionOptions::accounts)
          .extracting { it.name }
          .containsExactlyInAnyOrder("Amex", "Barclays", "Chase", "Mortgage")
        prop(TransactionOptions::categoryGroups)
          .extracting { group -> group.categories.map { it.name } }
          .containsExactly(listOf("Additional", "Building", "Car"))
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Edits change the draft until they're discarded`() = runDatabaseTest { scope ->
    insertEntities()
    AccountDao(this).insert(id = AccountId("d"), name = "Mortgage", offBudget = true)
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val viewModel = createViewModel(scope, id = "t")

    viewModel.state.test {
      awaitLoaded()
      viewModel.startEditing()
      awaitEdit()

      viewModel.setPayee(null)
      viewModel.setAccount(AccountId("d"))

      var edit = awaitEdit()
      while (edit.draft.account != AccountId("d")) edit = awaitEdit()
      assertThat(edit).all {
        prop(TransactionEditMode.Edit::draft)
          .isEqualTo(SAVED.copy(payee = null, account = AccountId("d")))
        prop(TransactionEditMode.Edit::payeeName).isNull()
        prop(TransactionEditMode.Edit::accountName).isEqualTo("Mortgage")
        prop(TransactionEditMode.Edit::isOffBudget).isTrue()
        prop(TransactionEditMode.Edit::hasChanges).isTrue()
      }

      viewModel.stopEditing()
      var loaded = awaitLoaded()
      while (loaded.mode != View) loaded = awaitLoaded()

      viewModel.startEditing()
      assertThat(awaitEdit().draft).isEqualTo(SAVED)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Saving writes the changed fields and goes back to viewing`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val sync = FakeSyncController()
    val viewModel = createViewModel(scope, id = "t", sync = sync)

    viewModel.state.test {
      awaitLoaded()
      viewModel.startEditing()
      awaitEdit()

      viewModel.setAmount(Amount(-25.0))
      viewModel.setCategory(null)
      viewModel.setNotes("Lunch")
      viewModel.save()

      var loaded = awaitLoaded()
      while (loaded.mode != View) loaded = awaitLoaded()
      assertThat(sync.calls)
        .containsExactly(
          listOf(
            LocalChange(TRANSACTIONS, "t", "category", Null),
            LocalChange(TRANSACTIONS, "t", "amount", MessageValue.Number(-2500)),
            LocalChange(TRANSACTIONS, "t", "notes", MessageValue.String("Lunch")),
          ),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Edits can't be discarded or changed while they're being saved`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val gate = CompletableDeferred<Unit>()
    val sync = FakeSyncController(gate = gate)
    val viewModel = createViewModel(scope, id = "t", sync = sync)

    viewModel.state.test {
      awaitLoaded()
      viewModel.startEditing()
      awaitEdit()

      viewModel.setNotes("Lunch")
      viewModel.save()
      var loaded = awaitLoaded()
      while (!loaded.isWorking) loaded = awaitLoaded()

      viewModel.stopEditing()
      viewModel.setNotes("Dinner")
      scope.advanceUntilIdle()
      expectNoEvents()

      gate.complete(Unit)
      while (loaded.mode != View) loaded = awaitLoaded()
      assertThat(sync.calls)
        .containsExactly(
          listOf(LocalChange(TRANSACTIONS, "t", "notes", MessageValue.String("Lunch"))),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Saving without changes writes nothing`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val sync = FakeSyncController()
    val viewModel = createViewModel(scope, id = "t", sync = sync)

    viewModel.state.test {
      awaitLoaded()
      viewModel.startEditing()
      awaitEdit()

      viewModel.save()

      var loaded = awaitLoaded()
      while (loaded.mode != View) loaded = awaitLoaded()
      assertThat(sync.calls).isEmpty()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A failed save is reported and keeps the edits`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val sync = FakeSyncController(failure = IllegalStateException("Offline"))
    val viewModel = createViewModel(scope, id = "t", sync = sync)

    viewModel.state.test {
      awaitLoaded()
      viewModel.startEditing()
      awaitEdit()
      viewModel.setCleared(false)
      assertThat(awaitEdit().draft).isEqualTo(SAVED.copy(cleared = false))

      viewModel.save()

      viewModel.error.test {
        var error = awaitItem()
        while (error == null) error = awaitItem()
        assertThat(error).isEqualTo(EditTransactionError.Saving("Offline"))
        cancelAndIgnoreRemainingEvents()
      }

      // Nothing since the save has left edit mode or touched the draft
      val modes =
        cancelAndConsumeRemainingEvents().filterIsInstance<Event.Item<EditTransactionState>>().map {
          (it.value as? EditTransactionState.Loaded)?.mode
        }
      assertThat(modes).each { mode ->
        mode
          .isNotNull()
          .isInstanceOf<TransactionEditMode.Edit>()
          .prop(TransactionEditMode.Edit::draft)
          .isEqualTo(SAVED.copy(cleared = false))
      }
    }
  }

  @Test
  fun `Deleting tombstones the transaction and reports it`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val sync = FakeSyncController()
    val viewModel = createViewModel(scope, id = "t", sync = sync)

    viewModel.events.test {
      viewModel.state.test {
        awaitLoaded()
        cancelAndIgnoreRemainingEvents()
      }
      viewModel.delete()

      assertThatNextEmissionIsEqualTo(Deleted)
      assertThat(sync.calls).containsExactly(listOf(tombstone(TRANSACTIONS, "t")))
    }
  }

  @Test
  fun `Splits can't be edited yet`() = runDatabaseTest { scope ->
    insertEntities()
    with(TransactionDao(this)) {
      insertTransaction("p", "a", null, "a", amount = 100.0, isParent = true)
      insertTransaction("p1", "a", "b", "b", amount = 100.0, parent = "p")
    }
    val viewModel = createViewModel(scope, id = "p")

    viewModel.state.test {
      assertThat(awaitLoaded().canEdit).isFalse()

      viewModel.startEditing()
      scope.advanceUntilIdle()

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Transfers can't be edited yet`() = runDatabaseTest { scope ->
    insertEntities()
    PayeeDao(this).insert(PayeeId("transfer"), name = null, transferAccount = AccountId("b"))
    TransactionDao(this)
      .insert(id = "t", account = "a", category = null, payee = "transfer", date = DATE_1)
    val viewModel = createViewModel(scope, id = "t")

    viewModel.state.test {
      assertThat(awaitLoaded().canEdit).isFalse()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private suspend fun ReceiveTurbine<EditTransactionState>.awaitEdit(): TransactionEditMode.Edit {
    var mode = awaitLoaded().mode
    while (mode !is Edit) mode = awaitLoaded().mode
    return mode
  }

  private suspend fun ReceiveTurbine<EditTransactionState>.awaitLoaded():
    EditTransactionState.Loaded {
    var state = awaitItem()
    while (state !is Loaded) state = awaitItem()
    return state
  }

  private suspend fun BudgetDatabase.insertEntities() {
    with(AccountDao(this)) {
      insertAccount(AccountId("a"), "Amex")
      insertAccount(AccountId("b"), "Barclays")
      insertAccount(AccountId("c"), "Chase")
    }
    with(PayeeDao(this)) {
      insertPayee(PayeeId("a"), "Argos")
      insertPayee(PayeeId("b"), "B&Q")
      insertPayee(PayeeId("c"), "Co-op")
    }
    with(CategoryDao(this)) {
      insertCategory(CategoryId("a"), "Additional")
      insertCategory(CategoryId("b"), "Building")
      insertCategory(CategoryId("c"), "Car")
    }
  }

  private fun BudgetDatabase.createViewModel(
    scope: TestScope,
    id: String,
    sync: FakeSyncController = FakeSyncController(),
  ): EditTransactionViewModel {
    Dispatchers.setMain(StandardTestDispatcher(scope.testScheduler))
    val accountDao = AccountDao(this)
    val categoryDao = CategoryDao(this)
    val payeeDao = PayeeDao(this)
    val transactionDao = TransactionDao(this)
    return EditTransactionViewModel(
      id = TransactionId(id),
      loader = TransactionLoader(transactionDao),
      writer =
        TransactionWriter(
          syncController = sync,
          accountDao = accountDao,
          categoryDao = categoryDao,
          payeeDao = payeeDao,
          transactionDao = transactionDao,
          uuidGenerator = { "uuid" },
          clock = Clock.System,
        ),
      accountDao = accountDao,
      payeeDao = payeeDao,
      categoryDao = categoryDao,
    )
  }

  // Records the changes it's sent, without applying them to the database
  private class FakeSyncController(
    private val failure: Exception? = null,
    private val gate: CompletableDeferred<Unit>? = null,
  ) : BudgetSyncController {
    val calls = mutableListOf<List<LocalChange>>()

    override suspend fun syncChanges(changes: List<LocalChange>) {
      gate?.await()
      failure?.let { throw it }
      calls += changes
    }

    override fun schedule() = Unit
  }

  private companion object {
    // What insertTransaction("t", account = "a", category = "a", payee = "a") stores
    val SAVED =
      TransactionFields(
        account = AccountId("a"),
        date = DATE_1,
        amount = Amount(123.45),
        payee = PayeeId("a"),
        category = CategoryId("a"),
        notes = null,
        cleared = true,
        reconciled = false,
      )
  }
}
