package aktual.budget.transactions.vm.edit

import aktual.budget.BudgetSyncController
import aktual.budget.budgeting.domain.BudgetMonthCalculatorImpl
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Categories
import aktual.budget.db.Rules
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.DatabaseTables.PAYEES
import aktual.budget.db.dao.DatabaseTables.PAYEE_MAPPING
import aktual.budget.db.dao.DatabaseTables.TAGS
import aktual.budget.db.dao.DatabaseTables.TRANSACTIONS
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.dao.SyncDao
import aktual.budget.db.dao.TagsDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.BudgetId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.DbMetadata.Companion.BudgetShowHiddenCategories
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.TagId
import aktual.budget.model.TransactionId
import aktual.budget.model.localChange
import aktual.budget.model.tombstone
import aktual.budget.rules.domain.TransactionRulesLoader
import aktual.budget.tags.domain.TagCreator
import aktual.budget.tags.domain.TagLabel
import aktual.budget.transactions.domain.LastTransaction
import aktual.budget.transactions.domain.SplitPart
import aktual.budget.transactions.domain.TransactionDraft
import aktual.budget.transactions.domain.TransactionFields
import aktual.budget.transactions.domain.TransactionLoader
import aktual.budget.transactions.domain.TransactionWriter
import aktual.budget.transactions.vm.DATE_1
import aktual.budget.transactions.vm.DATE_2
import aktual.budget.transactions.vm.asParent
import aktual.budget.transactions.vm.edit.EditTransactionState.Loaded
import aktual.budget.transactions.vm.edit.TransactionEditMode.Edit
import aktual.budget.transactions.vm.insertAccount
import aktual.budget.transactions.vm.insertCategory
import aktual.budget.transactions.vm.insertPayee
import aktual.budget.transactions.vm.insertTransaction
import aktual.budget.transactions.vm.transaction
import aktual.test.TestBudgetLocalPreferences
import aktual.test.TestCalendar
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.inMemoryDriverFactory
import aktual.test.runDatabaseTest
import aktual.test.viewModelContext
import alakazam.test.TestCoroutineContexts
import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.read
import androidx.savedstate.serialization.encodeToSavedState
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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("TooManyFunctions")
@RunWith(RobolectricTestRunner::class)
class EditTransactionViewModelTest {
  @Test
  fun `A transaction opens ready to edit`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val viewModel = createViewModel(scope, id = "t")

    viewModel.state.test {
      assertThat(awaitLoaded()).all {
        prop(Loaded::saved)
          .isEqualTo(
            TransactionDetails(
              transaction =
                transaction("t", account = "a", category = "a", payee = "a", balance = 123.45),
              cleared = true,
              reconciled = false,
            ),
          )
        prop(Loaded::canSave).isFalse()
        prop(Loaded::mode).isInstanceOf<Edit>().prop(Edit::form).all {
          prop(TransactionForm::draft).isEqualTo(SAVED)
          prop(TransactionForm::hasChanges).isFalse()
        }
      }
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
      assertThat(awaitLoaded().saved?.transaction)
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
      assertThat(awaitLoaded().saved?.transaction?.children?.size).isEqualTo(1)

      transactions.insertTransaction("p2", "a", "c", "b", amount = 40.0, parent = "p")
      scope.advanceUntilIdle()

      // An unchanged transaction's form follows too
      var loaded = awaitLoaded()
      while (loaded.mode.form.parts.size != 2) loaded = awaitLoaded()
      assertThat(loaded.saved?.transaction?.children?.size).isEqualTo(2)
      assertThat(loaded.mode.form.hasChanges).isFalse()
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
  fun `The form starts from what's saved`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val viewModel = createViewModel(scope, id = "t")

    viewModel.state.test {
      assertThat(awaitEdit()).all {
        prop(TransactionForm::draft).isEqualTo(SAVED)
        prop(TransactionForm::payeeName).isEqualTo("Argos")
        prop(TransactionForm::categoryName).isEqualTo("Additional")
        prop(TransactionForm::accountName).isEqualTo("Amex")
        prop(TransactionForm::isOffBudget).isFalse()
        prop(TransactionForm::hasChanges).isFalse()
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `The pickers offer open accounts, payees and transfers`() = runDatabaseTest { scope ->
    insertEntities()
    AccountDao(this).insert(id = AccountId("d"), name = "Mortgage", offBudget = true)
    PayeeDao(this).insert(PayeeId("transfer"), name = null, transferAccount = AccountId("b"))
    // The transaction's own account isn't one it can transfer to
    PayeeDao(this).insert(PayeeId("self"), name = null, transferAccount = AccountId("a"))
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val viewModel = createViewModel(scope, id = "t")

    viewModel.state.test {
      assertThat(awaitEdit().options).all {
        prop(TransactionOptions::payees)
          .extracting { it.name }
          .containsExactly("Argos", "B&Q", "Co-op")
        prop(TransactionOptions::transfers)
          .containsExactly(EntityOption(PayeeId("transfer"), "Barclays"))
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
  fun `The favourite then most used payees are suggested before the rest`() =
    runDatabaseTest { scope ->
      insertEntities()
      with(PayeeDao(this)) {
        insertPayee(PayeeId("d"), "Dunelm")
        insertPayee(PayeeId("e"), "Esso")
      }
      payeesQueries.withoutResult {
        insert(PayeeId("d"), "Dunelm", null, false, null, favorite = true, learn_categories = null)
      }
      with(TransactionDao(this)) {
        insertTransaction("t", account = "a", category = "a", payee = "c", date = TODAY)
        insertTransaction("t2", account = "a", category = "a", payee = "c", date = TODAY)
        insertTransaction("t3", account = "a", category = "a", payee = "b", date = TODAY)
        insertTransaction("t4", account = "a", category = "a", payee = "a", date = DATE_1)
      }
      val viewModel = createViewModel(scope, id = "t")

      viewModel.state.test {
        assertThat(awaitEdit().options).all {
          prop(TransactionOptions::suggestedPayees)
            .extracting { it.name }
            .containsExactly("Dunelm", "B&Q", "Co-op")
          prop(TransactionOptions::payees).extracting { it.name }.containsExactly("Argos", "Esso")
        }
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Edits change the draft until they're reverted`() = runDatabaseTest { scope ->
    insertEntities()
    AccountDao(this).insert(id = AccountId("d"), name = "Mortgage", offBudget = true)
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val viewModel = createViewModel(scope, id = "t")

    viewModel.state.test {
      awaitEdit()

      viewModel.setPayee(null)
      viewModel.setAccount(AccountId("d"))

      var edit = awaitEdit()
      while (edit.draft.account != AccountId("d")) edit = awaitEdit()
      assertThat(edit).all {
        prop(TransactionForm::draft).isEqualTo(SAVED.copy(payee = null, account = AccountId("d")))
        prop(TransactionForm::payeeName).isNull()
        prop(TransactionForm::accountName).isEqualTo("Mortgage")
        prop(TransactionForm::isOffBudget).isTrue()
        prop(TransactionForm::hasChanges).isTrue()
      }

      viewModel.revert()
      edit = awaitEdit()
      while (edit.hasChanges) edit = awaitEdit()
      assertThat(edit.draft).isEqualTo(SAVED)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Saving writes the changed fields and leaves nothing unsaved`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val sync = FakeSyncController()
    val viewModel = createViewModel(scope, id = "t", sync = sync)

    viewModel.state.test {
      val initial = awaitLoaded()

      viewModel.setAmount(Amount(-25.0))
      viewModel.setCategory(null)
      viewModel.setNotes("Lunch")
      viewModel.save()
      scope.advanceUntilIdle()

      // The form follows what's saved again, which can conflate back to how it started
      val states =
        cancelAndConsumeRemainingEvents().filterIsInstance<Event.Item<EditTransactionState>>()
      val last = states.lastOrNull()?.value ?: initial
      assertThat(last).isInstanceOf<Loaded>().all {
        prop(Loaded::isWorking).isFalse()
        prop("hasChanges") { it.mode.form.hasChanges }.isFalse()
      }
      assertThat(sync.calls)
        .containsExactly(
          listOf(
            LocalChange(TRANSACTIONS, "t", "category", Null),
            LocalChange(TRANSACTIONS, "t", "amount", MessageValue.Number(-2500)),
            LocalChange(TRANSACTIONS, "t", "notes", MessageValue.String("Lunch")),
          ),
        )
    }
  }

  @Test
  fun `Edits can't be discarded, reverted or changed while they're being saved`() =
    runDatabaseTest { scope ->
      insertEntities()
      TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
      val gate = CompletableDeferred<Unit>()
      val sync = FakeSyncController(gate = gate)
      val viewModel = createViewModel(scope, id = "t", sync = sync)

      viewModel.state.test {
        awaitEdit()

        viewModel.setNotes("Lunch")
        viewModel.save()
        var loaded = awaitLoaded()
        while (!loaded.isWorking) loaded = awaitLoaded()

        viewModel.discard()
        viewModel.revert()
        viewModel.setNotes("Dinner")
        scope.advanceUntilIdle()
        expectNoEvents()

        gate.complete(Unit)
        while (loaded.isWorking || loaded.mode.form.hasChanges) loaded = awaitLoaded()
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
      awaitEdit()

      viewModel.save()
      scope.advanceUntilIdle()

      expectNoEvents()
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
          (it.value as? Loaded)?.mode
        }
      assertThat(modes).each { mode ->
        mode
          .isNotNull()
          .isInstanceOf<Edit>()
          .prop(Edit::form)
          .prop(TransactionForm::draft)
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
  fun `Saving after a change elsewhere only writes what was edited`() =
    runSyncedTest { scope, elsewhere ->
      insertEntities()
      TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
      val sync = FakeSyncController()
      val viewModel = createViewModel(scope, id = "t", sync = sync)

      viewModel.state.test {
        awaitEdit()
        viewModel.setNotes("Lunch")
        awaitEdit()

        elsewhere.sendMessages(listOf(localChange(TRANSACTIONS, "t", "amount", -5000L)))
        scope.advanceUntilIdle()

        // The draft follows what it hasn't changed
        var loaded = awaitLoaded()
        while (loaded.mode.form.draft.amount != Amount(-50.0)) loaded = awaitLoaded()
        assertThat(loaded).all {
          prop(Loaded::changedElsewhere).isNull()
          prop("draft") { it.mode.form.draft }
            .isEqualTo(SAVED.copy(amount = Amount(-50.0), notes = "Lunch"))
        }

        viewModel.save()
        scope.advanceUntilIdle()
        assertThat(sync.calls)
          .containsExactly(
            listOf(LocalChange(TRANSACTIONS, "t", "notes", MessageValue.String("Lunch"))),
          )
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `A field changed both here and elsewhere is a conflict until reverted`() =
    runSyncedTest { scope, elsewhere ->
      insertEntities()
      TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
      val viewModel = createViewModel(scope, id = "t")

      viewModel.state.test {
        awaitEdit()
        viewModel.setPayee(PayeeId("b"))
        awaitEdit()

        elsewhere.sendMessages(listOf(localChange(TRANSACTIONS, "t", "description", "c")))
        scope.advanceUntilIdle()

        var loaded = awaitLoaded()
        while (loaded.changedElsewhere == null) loaded = awaitLoaded()
        assertThat(loaded).all {
          prop(Loaded::changedElsewhere).isEqualTo(Conflicting)
          prop(Loaded::canSave).isTrue()
          prop("payee") { it.mode.form.draft.payee }.isEqualTo(PayeeId("b"))
        }

        viewModel.revert()
        while (loaded.changedElsewhere != null) loaded = awaitLoaded()
        assertThat(loaded.mode.form.draft.payee).isEqualTo(PayeeId("c"))
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Saving a new payee isn't a change made elsewhere`() = runSyncedTest { scope, database ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    // Holds the save open once it's written, so what it wrote is seen before it finishes
    val gate = CompletableDeferred<Unit>()
    val sync = FakeSyncController(gate = gate, appliedTo = database)
    val viewModel = createViewModel(scope, id = "t", sync = sync, savedUuid = "saved")

    viewModel.state.test {
      awaitEdit()
      viewModel.createPayee("Deli")
      awaitEdit()

      viewModel.save()
      scope.advanceUntilIdle()
      gate.complete(Unit)
      scope.advanceUntilIdle()

      val states =
        cancelAndConsumeRemainingEvents()
          .asSequence()
          .filterIsInstance<Event.Item<EditTransactionState>>()
          .map { it.value }
          .filterIsInstance<Loaded>()
          .toList()
      assertThat(states.map { it.changedElsewhere }).each { it.isNull() }
      assertThat(states.last().mode.form.draft.payee).isEqualTo(PayeeId("saved"))
    }
  }

  @Test
  fun `A transaction deleted elsewhere keeps its draft, which can't be saved`() =
    runSyncedTest { scope, elsewhere ->
      insertEntities()
      TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
      val sync = FakeSyncController()
      val viewModel = createViewModel(scope, id = "t", sync = sync)

      viewModel.state.test {
        awaitEdit()
        viewModel.setNotes("Lunch")
        awaitEdit()

        elsewhere.sendMessages(listOf(tombstone(TRANSACTIONS, "t")))
        scope.advanceUntilIdle()

        var loaded = awaitLoaded()
        while (loaded.changedElsewhere == null) loaded = awaitLoaded()
        assertThat(loaded).all {
          prop(Loaded::changedElsewhere).isEqualTo(Deleted)
          prop(Loaded::canSave).isFalse()
          prop("notes") { it.mode.form.draft.notes }.isEqualTo("Lunch")
        }

        viewModel.save()
        viewModel.delete()
        scope.advanceUntilIdle()
        assertThat(sync.calls).isEmpty()

        // Without its draft, there's nothing left to show
        viewModel.revert()
        var state = awaitItem()
        while (state is Loaded) state = awaitItem()
        assertThat(state).isEqualTo(EditTransactionState.Failure.NotFound)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `A transaction deleted elsewhere without a draft isn't found`() =
    runSyncedTest { scope, elsewhere ->
      insertEntities()
      TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
      val viewModel = createViewModel(scope, id = "t")

      viewModel.state.test {
        awaitEdit()

        elsewhere.sendMessages(listOf(tombstone(TRANSACTIONS, "t")))
        scope.advanceUntilIdle()

        var state = awaitItem()
        while (state is Loaded) state = awaitItem()
        assertThat(state).isEqualTo(EditTransactionState.Failure.NotFound)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `A split is edited with its parts`() = runDatabaseTest { scope ->
    insertEntities()
    insertSplit()
    val viewModel = createViewModel(scope, id = "p")

    viewModel.state.test {
      assertThat(awaitEdit()).all {
        prop(TransactionForm::payeeName).isEqualTo("Argos")
        prop(TransactionForm::splitRemaining).isNull()
        prop(TransactionForm::hasChanges).isFalse()
        prop(TransactionForm::parts)
          .containsExactly(
            SplitPartForm(part("p1", 60.0, "b", "b"), payeeName = "B&Q", categoryName = "Building"),
            SplitPartForm(part("p2", 40.0, "b", "c"), payeeName = "B&Q", categoryName = "Car"),
          )
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Splitting moves the payee to two empty parts`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val sync = FakeSyncController()
    val viewModel = createViewModel(scope, id = "t", sync = sync)

    viewModel.state.test {
      awaitEdit()

      viewModel.split()

      var loaded = awaitLoaded()
      assertThat(loaded.canSave).isFalse()
      assertThat(loaded.mode).isInstanceOf<Edit>().prop(Edit::form).all {
        prop(TransactionForm::draft).isEqualTo(SAVED.copy(payee = null))
        // The split has no payee of its own now, so its parts' one stands in
        prop(TransactionForm::payeeName).isEqualTo("Argos")
        prop(TransactionForm::splitRemaining).isEqualTo(Amount(123.45))
        prop(TransactionForm::parts)
          .extracting { it.part }
          .containsExactly(
            SplitPart(TransactionId("part-1"), Zero, PayeeId("a"), CategoryId("a"), null, -1),
            SplitPart(TransactionId("part-2"), Zero, PayeeId("a"), CategoryId("a"), null, -2),
          )
      }

      // Saving does nothing while the parts don't add up
      viewModel.save()
      viewModel.setPartAmount(TransactionId("part-1"), Amount(123.45))

      loaded = awaitLoaded()
      assertThat(loaded.canSave).isTrue()

      viewModel.save()
      while (loaded.isWorking || loaded.mode.form.hasChanges) loaded = awaitLoaded()
      val columns = setOf("amount", "description", "isParent", "parent_id")
      assertThat(sync.calls.single().filter { it.column in columns })
        .containsExactly(
          LocalChange(TRANSACTIONS, "t", "description", Null),
          localChange(TRANSACTIONS, "t", "isParent", true),
          localChange(TRANSACTIONS, "part-1", "amount", 12345L),
          localChange(TRANSACTIONS, "part-1", "description", "a"),
          localChange(TRANSACTIONS, "part-1", "parent_id", "t"),
          localChange(TRANSACTIONS, "part-2", "amount", 0L),
          localChange(TRANSACTIONS, "part-2", "description", "a"),
          localChange(TRANSACTIONS, "part-2", "parent_id", "t"),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `An added part can take what's left to assign`() = runDatabaseTest { scope ->
    insertEntities()
    insertSplit()
    val sync = FakeSyncController()
    val viewModel = createViewModel(scope, id = "p", sync = sync)

    viewModel.state.test {
      awaitEdit()

      viewModel.addSplit()
      // As upstream, a new part starts with the payee of the one before it
      assertThat(awaitEdit().parts.last())
        .isEqualTo(
          SplitPartForm(
            part =
              SplitPart(
                id = TransactionId("part-1"),
                payee = PayeeId("b"),
                sortOrder = SORT_ORDER - 1,
              ),
            payeeName = "B&Q",
            categoryName = null,
          ),
        )

      viewModel.setPartAmount(TransactionId("p2"), Amount(30.0))
      assertThat(awaitEdit().splitRemaining).isEqualTo(Amount(10.0))

      viewModel.fillRemaining(TransactionId("part-1"))
      assertThat(awaitEdit()).all {
        prop(TransactionForm::splitRemaining).isNull()
        prop(TransactionForm::parts)
          .extracting { it.part.amount }
          .containsExactly(Amount(60.0), Amount(30.0), Amount(10.0))
      }

      viewModel.save()
      var loaded = awaitLoaded()
      while (loaded.isWorking || loaded.mode.form.hasChanges) loaded = awaitLoaded()
      assertThat(sync.calls.single().filter { it.column in setOf("amount", "parent_id") })
        .containsExactly(
          localChange(TRANSACTIONS, "p2", "amount", 3000L),
          localChange(TRANSACTIONS, "part-1", "amount", 1000L),
          localChange(TRANSACTIONS, "part-1", "parent_id", "p"),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Removing the last part leaves an ordinary transaction`() = runDatabaseTest { scope ->
    insertEntities()
    with(TransactionDao(this)) {
      insertTransaction("p", "a", null, "a", amount = 100.0, isParent = true)
      insertTransaction("p1", "a", "b", "b", amount = 100.0, parent = "p")
    }
    val sync = FakeSyncController()
    val viewModel = createViewModel(scope, id = "p", sync = sync)

    viewModel.state.test {
      awaitEdit()

      viewModel.removeSplit(TransactionId("p1"))
      assertThat(awaitEdit()).all {
        prop(TransactionForm::isSplit).isFalse()
        prop(TransactionForm::hasChanges).isTrue()
      }

      viewModel.save()
      var loaded = awaitLoaded()
      while (loaded.isWorking || loaded.mode.form.hasChanges) loaded = awaitLoaded()
      assertThat(sync.calls)
        .containsExactly(
          listOf(
            localChange(TRANSACTIONS, "p", "isParent", false),
            tombstone(TRANSACTIONS, "p1"),
          ),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A part's fields are edited on their own`() = runDatabaseTest { scope ->
    insertEntities()
    insertSplit()
    val viewModel = createViewModel(scope, id = "p")

    viewModel.state.test {
      awaitEdit()

      viewModel.setPartCategory(TransactionId("p1"), CategoryId("a"))
      viewModel.setPartPayee(TransactionId("p1"), PayeeId("c"))
      viewModel.setPartNotes(TransactionId("p1"), "Paint")

      var edit = awaitEdit()
      while (edit.parts.first().part.notes == null) edit = awaitEdit()
      assertThat(edit.parts)
        .containsExactly(
          SplitPartForm(
            part = part("p1", 60.0, "c", "a").copy(notes = "Paint"),
            payeeName = "Co-op",
            categoryName = "Additional",
          ),
          SplitPartForm(part("p2", 40.0, "b", "c"), payeeName = "B&Q", categoryName = "Car"),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Saving a new split inserts the parent and its parts`() = runDatabaseTest { scope ->
    insertEntities()
    val sync = FakeSyncController()
    val target = EditTransactionTarget.New(AccountId("a"))
    val viewModel = createViewModel(scope, sync = sync, target = target)

    viewModel.events.test {
      viewModel.state.test {
        awaitCreate()
        cancelAndIgnoreRemainingEvents()
      }
      viewModel.setAmount(Amount(-25.0))
      viewModel.split()
      viewModel.setPartAmount(TransactionId("part-1"), Amount(-20.0))
      viewModel.fillRemaining(TransactionId("part-2"))
      viewModel.save()

      assertThatNextEmissionIsEqualTo(Created)
      val columns = setOf("amount", "isParent", "isChild", "parent_id")
      assertThat(sync.calls.single().filter { it.column in columns })
        .containsExactly(
          localChange(TRANSACTIONS, "uuid", "amount", -2500L),
          localChange(TRANSACTIONS, "uuid", "isParent", 1L),
          localChange(TRANSACTIONS, "part-1", "amount", -2000L),
          localChange(TRANSACTIONS, "part-1", "isChild", 1L),
          localChange(TRANSACTIONS, "part-1", "parent_id", "uuid"),
          localChange(TRANSACTIONS, "part-2", "amount", -500L),
          localChange(TRANSACTIONS, "part-2", "isChild", 1L),
          localChange(TRANSACTIONS, "part-2", "parent_id", "uuid"),
        )
    }
  }

  @Test
  fun `A transfer between on budget accounts has no category`() = runDatabaseTest { scope ->
    insertEntities()
    AccountDao(this).insert(id = AccountId("d"), name = "Mortgage", offBudget = true)
    PayeeDao(this).insert(PayeeId("transfer"), name = null, transferAccount = AccountId("b"))
    PayeeDao(this).insert(PayeeId("mortgage"), name = null, transferAccount = AccountId("d"))
    TransactionDao(this)
      .insert(id = "t", account = "a", category = null, payee = "transfer", date = DATE_1)
    val viewModel = createViewModel(scope, id = "t")

    viewModel.state.test {
      assertThat(awaitEdit()).all {
        prop(TransactionForm::payeeName).isEqualTo("Barclays")
        prop(TransactionForm::isTransfer).isTrue()
      }

      // Money leaving the budget is categorised
      viewModel.setPayee(PayeeId("mortgage"))
      assertThat(awaitEdit().isTransfer).isFalse()

      viewModel.setPayee(PayeeId("a"))
      assertThat(awaitEdit().isTransfer).isFalse()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Moving into the account a transfer goes to drops the transfer`() = runDatabaseTest { scope ->
    insertEntities()
    PayeeDao(this).insert(PayeeId("to-b"), name = null, transferAccount = AccountId("b"))
    with(TransactionDao(this)) {
      insertTransaction("p", "a", null, "a", amount = 100.0, isParent = true)
      insert("p1", "a", null, "to-b", DATE_1, amount = 60.0, parent = "p")
      insertTransaction("p2", "a", "c", "c", amount = 40.0, parent = "p")
    }
    val viewModel = createViewModel(scope, id = "p")

    viewModel.state.test {
      awaitEdit()

      viewModel.setAccount(AccountId("b"))

      var edit = awaitEdit()
      while (edit.parts.first().part.payee != null) edit = awaitEdit()
      assertThat(edit).all {
        prop("account") { it.draft.account }.isEqualTo(AccountId("b"))
        prop("payee") { it.draft.payee }.isEqualTo(PayeeId("a"))
        prop(TransactionForm::parts)
          .extracting { it.part.payee to it.isTransfer }
          .containsExactly(null to false, PayeeId("c") to false)
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Deleting a transfer deletes its other side`() = runDatabaseTest { scope ->
    insertEntities()
    PayeeDao(this).insert(PayeeId("to-b"), name = null, transferAccount = AccountId("b"))
    PayeeDao(this).insert(PayeeId("to-a"), name = null, transferAccount = AccountId("a"))
    with(TransactionDao(this)) {
      insert("t", "a", null, "to-b", DATE_1, amount = -10.0, transferId = "o")
      insert("o", "b", null, "to-a", DATE_1, amount = 10.0, transferId = "t")
    }
    val sync = FakeSyncController()
    val viewModel = createViewModel(scope, id = "t", sync = sync)

    viewModel.events.test {
      viewModel.state.test {
        assertThat(awaitLoaded().saved?.hasReconciledTransfer).isEqualTo(false)
        cancelAndIgnoreRemainingEvents()
      }
      viewModel.delete()

      assertThatNextEmissionIsEqualTo(Deleted)
      assertThat(sync.calls)
        .containsExactly(
          listOf(tombstone(TRANSACTIONS, "t")),
          listOf(
            tombstone(TRANSACTIONS, "o"),
            LocalChange(TRANSACTIONS, "t", "transferred_id", Null),
          ),
        )
    }
  }

  @Test
  fun `A new transaction starts in the list's account and category`() = runDatabaseTest { scope ->
    insertEntities()
    val target = EditTransactionTarget.New(AccountId("b"), CategoryId("c"))
    val viewModel = createViewModel(scope, target = target)

    viewModel.state.test {
      assertThat(awaitCreate()).all {
        prop(TransactionForm::draft)
          .isEqualTo(NEW.copy(account = AccountId("b"), category = CategoryId("c")))
        prop(TransactionForm::accountName).isEqualTo("Barclays")
        prop(TransactionForm::categoryName).isEqualTo("Car")
        prop(TransactionForm::hasChanges).isFalse()
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A new transaction follows the last one added`() = runDatabaseTest { scope ->
    insertEntities()
    val last = LastTransaction()
    last.set(SAVED.copy(account = AccountId("c"), date = DATE_2))
    val viewModel =
      createViewModel(scope, target = EditTransactionTarget.New(), lastTransaction = last)

    viewModel.state.test {
      assertThat(awaitCreate().draft).isEqualTo(NEW.copy(account = AccountId("c"), date = DATE_2))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A new transaction can't be saved without an account`() = runDatabaseTest { scope ->
    insertEntities()
    val viewModel = createViewModel(scope, target = EditTransactionTarget.New())

    viewModel.state.test {
      assertThat(awaitLoaded().canSave).isFalse()

      viewModel.setAccount(AccountId("a"))

      assertThat(awaitLoaded().canSave).isTrue()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A new transaction needs an account to go in`() = runDatabaseTest { scope ->
    CategoryDao(this).insertCategory(CategoryId("a"), "Additional")
    val viewModel = createViewModel(scope, target = EditTransactionTarget.New())

    viewModel.state.test {
      var state = awaitItem()
      while (state == Loading) state = awaitItem()
      assertThat(state).isEqualTo(EditTransactionState.Empty.NoAccounts)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A new transaction needs a category to pick`() = runDatabaseTest { scope ->
    AccountDao(this).insertAccount(AccountId("a"), "Amex")
    val viewModel = createViewModel(scope, target = EditTransactionTarget.New())

    viewModel.state.test {
      var state = awaitItem()
      while (state == Loading) state = awaitItem()
      assertThat(state).isEqualTo(EditTransactionState.Empty.NoCategories)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A new transaction can pick a category that's hidden`() = runDatabaseTest { scope ->
    AccountDao(this).insertAccount(AccountId("a"), "Amex")
    insertHiddenCategory("d", "Drinks")
    val viewModel = createViewModel(scope, target = EditTransactionTarget.New())

    viewModel.state.test {
      assertThat(awaitCreate().options).all {
        prop(TransactionOptions::hasHiddenCategories).isTrue()
        prop(TransactionOptions::categoryGroups).isEmpty()
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Rules fill in what a new transaction is missing`() = runDatabaseTest { scope ->
    insertEntities()
    insertCategoryRule(payee = "a", category = "b")
    val target = EditTransactionTarget.New(AccountId("a"))
    val viewModel = createViewModel(scope, target = target)

    viewModel.state.test {
      awaitCreate()
      viewModel.setPayee(PayeeId("a"))

      val filled = NEW.copy(account = AccountId("a"), payee = PayeeId("a"))
      assertThat(awaitCreate()).all {
        prop(TransactionForm::draft).isEqualTo(filled.copy(category = CategoryId("b")))
        prop(TransactionForm::categoryName).isEqualTo("Building")
      }

      // Taking the category off again is the user's choice, so later runs leave it off
      viewModel.setCategory(null)
      assertThat(awaitCreate().draft).isEqualTo(filled)

      viewModel.setNotes("Lunch")
      assertThat(awaitCreate().draft).isEqualTo(filled.copy(notes = "Lunch"))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Rules leave a saved transaction alone`() = runDatabaseTest { scope ->
    insertEntities()
    insertCategoryRule(payee = "b", category = "b")
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val viewModel = createViewModel(scope, id = "t")

    viewModel.state.test {
      awaitEdit()

      viewModel.setPayee(PayeeId("b"))

      assertThat(awaitEdit().draft).isEqualTo(SAVED.copy(payee = PayeeId("b")))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Saving a new transaction inserts it and closes`() = runDatabaseTest { scope ->
    insertEntities()
    val sync = FakeSyncController()
    val last = LastTransaction()
    val target = EditTransactionTarget.New(AccountId("a"))
    val viewModel = createViewModel(scope, sync = sync, target = target, lastTransaction = last)

    viewModel.events.test {
      viewModel.state.test {
        awaitCreate()
        cancelAndIgnoreRemainingEvents()
      }
      viewModel.setAmount(Amount(-25.0))
      viewModel.setPayee(PayeeId("a"))
      viewModel.setDate(DATE_2)
      viewModel.save()

      assertThatNextEmissionIsEqualTo(Created)
      assertThat(sync.calls.single().filter { it.column != "sort_order" })
        .containsExactly(
          localChange(TRANSACTIONS, "uuid", "acct", "a"),
          localChange(TRANSACTIONS, "uuid", "amount", -2500L),
          localChange(TRANSACTIONS, "uuid", "description", "a"),
          localChange(TRANSACTIONS, "uuid", "date", 20250602L),
          localChange(TRANSACTIONS, "uuid", "cleared", false),
          localChange(TRANSACTIONS, "uuid", "reconciled", false),
        )
      assertThat(last.fields).isNotNull().all {
        prop(TransactionFields::account).isEqualTo(AccountId("a"))
        prop(TransactionFields::date).isEqualTo(DATE_2)
      }
    }
  }

  @Test
  fun `A payee added by name is created with the transaction`() = runDatabaseTest { scope ->
    insertEntities()
    val sync = FakeSyncController()
    val target = EditTransactionTarget.New(AccountId("a"))
    val viewModel = createViewModel(scope, sync = sync, target = target)

    viewModel.events.test {
      viewModel.state.test {
        awaitCreate()

        // A name that's already taken picks that payee
        viewModel.createPayee("argos")
        assertThat(awaitCreate().draft.payee).isEqualTo(PayeeId("a"))

        viewModel.createPayee(" Deli ")
        assertThat(awaitCreate().payeeName).isEqualTo("Deli")
        cancelAndIgnoreRemainingEvents()
      }
      viewModel.save()

      assertThatNextEmissionIsEqualTo(Created)
      val changes = sync.calls.single()
      assertThat(changes.filter { it.dataset != TRANSACTIONS || it.column == "description" })
        .containsExactly(
          localChange(PAYEES, "uuid", "name", "Deli"),
          localChange(PAYEE_MAPPING, "uuid", "targetId", "uuid"),
          localChange(TRANSACTIONS, "uuid", "description", "uuid"),
        )
    }
  }

  @Test
  fun `Discarding a new transaction closes without writing`() = runDatabaseTest { scope ->
    insertEntities()
    val sync = FakeSyncController()
    val target = EditTransactionTarget.New(AccountId("a"))
    val viewModel = createViewModel(scope, sync = sync, target = target)

    viewModel.events.test {
      viewModel.state.test {
        awaitCreate()
        cancelAndIgnoreRemainingEvents()
      }
      viewModel.setNotes("Lunch")
      viewModel.discard()

      assertThatNextEmissionIsEqualTo(Discarded)
      assertThat(sync.calls).isEmpty()
    }
  }

  @Test
  fun `Discarding a saved transaction's edits closes without writing`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val sync = FakeSyncController()
    val viewModel = createViewModel(scope, id = "t", sync = sync)

    viewModel.events.test {
      viewModel.state.test {
        awaitEdit()
        viewModel.setNotes("Lunch")
        assertThat(awaitEdit().hasChanges).isTrue()
        cancelAndIgnoreRemainingEvents()
      }
      viewModel.discard()

      assertThatNextEmissionIsEqualTo(Discarded)
      assertThat(sync.calls).isEmpty()
    }
  }

  @Test
  fun `The notes offer the tags by name`() = runDatabaseTest { scope ->
    insertEntities()
    with(tagsDao(scope)) {
      insert(id = TagId("f"), tag = "food", color = "#FF0000", description = null)
      insert(id = TagId("b"), tag = "Bills", color = null, description = null)
    }
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val viewModel = createViewModel(scope, id = "t")

    viewModel.state.test {
      var tags = awaitEdit().options.tags
      while (tags.size < 2) tags = awaitEdit().options.tags
      assertThat(tags)
        .containsExactly(TagLabel(TagId("b"), "Bills", null), TagLabel(TagId("f"), "food", Red))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A tag created from the notes is synced and offered`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val sync = FakeSyncController()
    val viewModel = createViewModel(scope, id = "t", sync = sync)

    viewModel.state.test {
      awaitEdit()

      viewModel.createTag("food")
      var tags = awaitEdit().options.tags
      while (tags.isEmpty()) tags = awaitEdit().options.tags
      assertThat(tags.map { it.name to it.color }).containsExactly("food" to null)
      assertThat(sync.calls)
        .containsExactly(
          listOf(
            LocalChange(TAGS, "tag", "id", MessageValue.String("tag")),
            LocalChange(TAGS, "tag", "tag", MessageValue.String("food")),
            LocalChange(TAGS, "tag", "color", Null),
            LocalChange(TAGS, "tag", "description", Null),
            LocalChange(TAGS, "tag", "tombstone", MessageValue.Number(0)),
          ),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Creating a tag that exists keeps it as it is`() = runDatabaseTest { scope ->
    insertEntities()
    val tags = tagsDao(scope)
    tags.insert(id = TagId("f"), tag = "food", color = "#FF0000", description = "Meals")
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val sync = FakeSyncController()
    val viewModel = createViewModel(scope, id = "t", sync = sync)

    viewModel.createTag("food")
    scope.advanceUntilIdle()

    assertThat(sync.calls).isEmpty()
    assertThat(tags.getTag(TagId("f"))?.color).isEqualTo("#FF0000")
  }

  @Test
  fun `A tag that fails to sync shows an error`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val sync = FakeSyncController(failure = IllegalStateException("Offline"))
    val viewModel = createViewModel(scope, id = "t", sync = sync)

    viewModel.error.test {
      assertThat(awaitItem()).isNull()
      viewModel.createTag("food")
      assertThat(awaitItem()).isEqualTo(EditTransactionError.CreatingTag("Offline"))
      cancelAndIgnoreRemainingEvents()
    }
  }

  // Like runDatabaseTest, with a SyncDao that makes changes as a sync would
  private fun runSyncedTest(action: suspend BudgetDatabase.(TestScope, SyncDao) -> Unit) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use {
      val database = buildDatabase(driver)
      action(database, this, SyncDao(database, driver, Clock.System))
    }
  }

  @Test
  fun `Hidden categories are only offered while they're shown`() = runDatabaseTest { scope ->
    insertEntities()
    insertHiddenCategory("d", "Drinks")
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val prefs = TestBudgetLocalPreferences()
    val viewModel = createViewModel(scope, id = "t", prefs = prefs)

    viewModel.state.test {
      assertThat(awaitEdit().options).all {
        prop(TransactionOptions::hasHiddenCategories).isTrue()
        prop(TransactionOptions::showHiddenCategories).isFalse()
        prop(TransactionOptions::categoryGroups)
          .extracting { group -> group.categories.map { it.name } }
          .containsExactly(listOf("Additional", "Building", "Car"))
      }

      viewModel.toggleHiddenCategories()

      var form = awaitEdit()
      while (!form.options.showHiddenCategories) form = awaitEdit()
      assertThat(form.options.categoryGroups)
        .extracting { group -> group.categories.map { it.name to it.isHidden } }
        .containsExactly(
          listOf("Additional" to false, "Building" to false, "Car" to false, "Drinks" to true),
        )
      cancelAndIgnoreRemainingEvents()
    }
    assertThat(prefs.value[BudgetShowHiddenCategories]).isEqualTo(true)
  }

  @Test
  fun `Categories show their balance in the transaction's month while they're picked`() = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use {
      val database = buildDatabase(driver)
      BUDGET.forEach { sql -> driver.execute(identifier = null, sql = sql, parameters = 0).await() }
      AccountDao(database).insertAccount(AccountId("a"), "Amex")
      PayeeDao(database).insertPayee(PayeeId("a"), "Argos")
      TransactionDao(database)
        .insertTransaction("t", account = "a", category = "food", payee = "a", amount = -40.0)
      val viewModel = database.createViewModel(this, id = "t")

      viewModel.state.test {
        val closed = awaitEdit()
        assertThat(closed.options.categoryGroups.flatMap { it.categories }.map { it.balance })
          .each { it.isNull() }
        testScheduler.advanceUntilIdle()
        expectNoEvents()

        viewModel.setPickingCategory(true)

        // Income categories have no balance
        assertThat(awaitBalances { it != Zero })
          .containsExactly("Food" to Amount(10.0), "Rent" to Amount.Zero, "Salary" to null)

        // Before the transaction and its budget
        viewModel.setDate(LocalDate(2025, 5, 31))

        assertThat(awaitBalances { it == Zero })
          .containsExactly("Food" to Amount.Zero, "Rent" to Amount.Zero, "Salary" to null)
        cancelAndIgnoreRemainingEvents()
      }
    }
  }

  @Test
  fun `An edit outlives the process`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val savedState = SavedStateHandle()
    val first = createViewModel(scope, id = "t", savedState = savedState)
    first.state.test {
      awaitEdit()
      // The rules, which add a payee by name, load alongside
      scope.advanceUntilIdle()
      first.setNotes("Lunch")
      first.createPayee("Tesco")
      awaitEdit()
      cancelAndIgnoreRemainingEvents()
    }

    val restored = savedState.afterProcessDeath()
    val second = createViewModel(scope, id = "t", savedState = restored)

    second.state.test {
      assertThat(awaitEdit()).all {
        prop(TransactionForm::draft).isEqualTo(SAVED.copy(notes = "Lunch", payee = PayeeId("uuid")))
        prop(TransactionForm::payeeName).isEqualTo("Tesco")
        prop(TransactionForm::hasChanges).isTrue()
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A new transaction outlives the process`() = runDatabaseTest { scope ->
    insertEntities()
    val draft = NEW.copy(account = AccountId("b"), amount = Amount(-5.0))
    val snapshot =
      EditorSnapshot(
        draft = TransactionDraft(draft).toSnapshot(),
        base = TransactionDraft(NEW).toSnapshot(),
      )
    val savedState = SavedStateHandle(mapOf("draft" to encodeToSavedState(snapshot)))
    val viewModel =
      createViewModel(scope, target = EditTransactionTarget.New(), savedState = savedState)

    viewModel.state.test {
      assertThat(awaitCreate()).all {
        prop(TransactionForm::draft).isEqualTo(draft)
        prop(TransactionForm::hasChanges).isTrue()
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `An amount typed before the process died still runs the rules`() = runDatabaseTest { scope ->
    insertEntities()
    val savedState = SavedStateHandle()
    val first =
      createViewModel(scope, target = EditTransactionTarget.New(), savedState = savedState)
    first.state.test {
      awaitCreate()
      first.setPayee(PayeeId("a"))
      awaitCreate()
      first.setAmount(Amount(-5.0))
      awaitCreate()
      cancelAndIgnoreRemainingEvents()
    }

    insertCategoryRule(payee = "a", category = "b")
    val restored = savedState.afterProcessDeath()
    val second = createViewModel(scope, target = EditTransactionTarget.New(), savedState = restored)

    second.state.test {
      assertThat(awaitCreate().draft.category).isNull()
      second.commitAmount()
      assertThat(awaitCreate().draft.category).isEqualTo(CategoryId("b"))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A saved state that can't be read is ignored`() = runDatabaseTest { scope ->
    insertEntities()
    TransactionDao(this).insertTransaction("t", account = "a", category = "a", payee = "a")
    val savedState = SavedStateHandle(mapOf("draft" to encodeToSavedState(listOf("junk"))))
    val viewModel = createViewModel(scope, id = "t", savedState = savedState)

    viewModel.state.test {
      assertThat(awaitEdit()).all {
        prop(TransactionForm::draft).isEqualTo(SAVED)
        prop(TransactionForm::hasChanges).isFalse()
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  // As the saved state's restored after the process is killed. savedStateProvider() is restricted
  // to androidx.lifecycle, but there's no public way to save a handle on its own
  @Suppress("RestrictedApi")
  private fun SavedStateHandle.afterProcessDeath(): SavedStateHandle {
    val saved = savedStateProvider().saveState().read { getSavedState("draft") }
    return SavedStateHandle(mapOf("draft" to saved))
  }

  private suspend fun ReceiveTurbine<EditTransactionState>.awaitBalances(
    until: (Amount) -> Boolean,
  ): List<Pair<String, Amount?>> {
    while (true) {
      val categories = awaitEdit().options.categoryGroups.flatMap { it.categories }
      val first = categories.firstOrNull()?.balance
      if (first != null && until(first)) return categories.map { it.name to it.balance }
    }
  }

  private suspend fun BudgetDatabase.insertHiddenCategory(id: String, name: String) =
    categoriesQueries.insert(
      Categories(
        id = CategoryId(id),
        name = name,
        is_income = false,
        cat_group = null,
        sort_order = null,
        tombstone = false,
        hidden = true,
        goal_def = null,
        template_settings = null,
        cleanup_def = null,
      ),
    )

  private suspend fun ReceiveTurbine<EditTransactionState>.awaitEdit(): TransactionForm {
    var mode = awaitLoaded().mode
    while (mode !is Edit) mode = awaitLoaded().mode
    return mode.form
  }

  private suspend fun ReceiveTurbine<EditTransactionState>.awaitCreate(): TransactionForm {
    var mode = awaitLoaded().mode
    while (mode !is Create) mode = awaitLoaded().mode
    return mode.form
  }

  private suspend fun ReceiveTurbine<EditTransactionState>.awaitLoaded(): Loaded {
    var state = awaitItem()
    while (state !is Loaded) state = awaitItem()
    return state
  }

  // 100.00 split into parts of 60.00 and 40.00
  private suspend fun BudgetDatabase.insertSplit() =
    with(TransactionDao(this)) {
      insertTransaction("p", "a", null, "a", amount = 100.0, isParent = true)
      insertTransaction("p1", "a", "b", "b", amount = 60.0, parent = "p")
      insertTransaction("p2", "a", "c", "b", amount = 40.0, parent = "p")
    }

  private fun part(id: String, amount: Double, payee: String, category: String) =
    SplitPart(
      id = TransactionId(id),
      amount = Amount(amount),
      payee = PayeeId(payee),
      category = CategoryId(category),
      sortOrder = SORT_ORDER,
    )

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

  // Gives transactions with this payee the category, if they have none
  private suspend fun BudgetDatabase.insertCategoryRule(payee: String, category: String) =
    RulesDao(this)
      .insert(
        Rules(
          id = RuleId("rule"),
          stage = null,
          conditions =
            listOf(Condition(field = Payee, operator = Is, value = JsonPrimitive(payee))),
          actions =
            listOf(
              RuleAction(value = JsonPrimitive(category), op = RuleAction.Op.Set, field = Category),
            ),
          tombstone = false,
          conditions_op = And,
        ),
      )

  private fun BudgetDatabase.createViewModel(
    scope: TestScope,
    id: String = "t",
    sync: FakeSyncController = FakeSyncController(),
    target: EditTransactionTarget = EditTransactionTarget.Existing(TransactionId(id)),
    lastTransaction: LastTransaction = LastTransaction(),
    savedUuid: String = "uuid",
    savedState: SavedStateHandle = SavedStateHandle(),
    prefs: TestBudgetLocalPreferences = TestBudgetLocalPreferences(),
  ): EditTransactionViewModel {
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
    val tags = tagsDao(scope)
    val accountDao = AccountDao(this)
    val categoryDao = CategoryDao(this)
    val payeeDao = PayeeDao(this)
    val transactionDao = TransactionDao(this)
    var nextPart = 1
    return EditTransactionViewModel(
      target = target,
      savedState = savedState,
      loader = TransactionLoader(transactionDao),
      writer =
        TransactionWriter(
          syncController = sync,
          accountDao = accountDao,
          categoryDao = categoryDao,
          payeeDao = payeeDao,
          transactionDao = transactionDao,
          categoryLearner = {},
          uuidGenerator = { savedUuid },
          clock = Clock.System,
        ),
      rulesLoader =
        TransactionRulesLoader(
          rulesDao = RulesDao(this),
          contextDao = RuleContextDao(this),
          uuidGenerator = { "uuid" },
        ),
      lastTransaction = lastTransaction,
      calendar = TestCalendar(TODAY),
      accountDao = accountDao,
      payeeDao = payeeDao,
      categoryDao = categoryDao,
      uuidGenerator = { "part-${nextPart++}" },
      tagCreator = TagCreator(tagsDao = tags, uuidGenerator = { "tag" }, syncController = sync),
      localPreferences = prefs,
      budgetCalculator =
        BudgetMonthCalculatorImpl(
          budgetDao = BudgetDao(this, contexts),
          preferencesDao = PreferencesDao(this, contexts),
          calendar = TestCalendar(TODAY),
          contexts = contexts,
        ),
      tagsDao = tags,
      viewModelContext = scope.viewModelContext,
    )
  }

  private fun BudgetDatabase.tagsDao(scope: TestScope) =
    TagsDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))

  // Records the changes it's sent, without applying them to the database unless it's given one
  private class FakeSyncController(
    private val failure: Exception? = null,
    private val gate: CompletableDeferred<Unit>? = null,
    private val appliedTo: SyncDao? = null,
  ) : BudgetSyncController {
    val calls = mutableListOf<List<LocalChange>>()

    override suspend fun syncChanges(changes: List<LocalChange>) {
      appliedTo?.sendMessages(changes)
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

    val TODAY = LocalDate(2026, 10, 11)

    // Food has 50.00 budgeted in June 2025
    @Suppress("MaxLineLength")
    val BUDGET =
      listOf(
        "INSERT INTO category_groups(id, name, is_income, sort_order) VALUES ('usual', 'Usual', 0, 1), ('income', 'Income', 1, 2)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('food', 'Food', 0, 'usual', 1), ('rent', 'Rent', 0, 'usual', 2), ('salary', 'Salary', 1, 'income', 1)",
        "INSERT INTO category_mapping(id, transferId) VALUES ('food', 'food'), ('rent', 'rent'), ('salary', 'salary')",
        "INSERT INTO zero_budgets(id, month, category, amount) VALUES ('202506-food', 202506, 'food', 5000)",
      )

    // What insertTransaction() gives a row on DATE_1
    val SORT_ORDER = DATE_1.toEpochDays().days.inWholeMilliseconds

    // What a new transaction starts as, before its list or the last one added fill anything in
    val NEW =
      TransactionFields(
        account = null,
        date = TODAY,
        amount = Zero,
        payee = null,
        category = null,
        notes = null,
        cleared = false,
        reconciled = false,
      )
  }
}
