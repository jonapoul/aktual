package aktual.budget.payees.vm.edit

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Payees
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.PayeeId
import aktual.budget.payees.vm.PayeesLoader
import aktual.budget.payees.vm.list.PayeeItem
import aktual.budget.rules.domain.CategoryLearningPref
import aktual.budget.rules.domain.PayeeRuleCounter
import aktual.test.TestSyncController
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import alakazam.test.standardDispatcher
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import kotlin.test.AfterTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EditPayeeViewModelTest {
  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Loads the payee`() = runDatabaseTest { scope ->
    // given
    PayeeDao(this).insert(TESCO.id, TESCO.name)

    // then
    createViewModel(scope, TESCO.id).state.test {
      assertThatNextEmissionIsEqualTo(Loading)
      assertThatNextEmissionIsEqualTo(Editing(TESCO))
    }
  }

  @Test
  fun `A missing payee is a failure`() = runDatabaseTest { scope ->
    createViewModel(scope, PayeeId("missing")).state.test {
      assertThatNextEmissionIsEqualTo(Loading)
      assertThatNextEmissionIsEqualTo(Failure(cause = null))
    }
  }

  @Test
  fun `Tracks unsaved changes against the loaded payee`() = runDatabaseTest { scope ->
    // given
    PayeeDao(this).insert(TESCO.id, TESCO.name)
    val viewModel = createViewModel(scope, TESCO.id).awaitLoaded()

    viewModel.hasUnsavedChanges.test {
      assertThatNextEmissionIsEqualTo(false)

      // when
      viewModel.setName("Tesco Extra")
      assertThatNextEmissionIsEqualTo(true)

      viewModel.setName("Tesco")
      assertThatNextEmissionIsEqualTo(false)

      viewModel.setFavorite(true)
      assertThatNextEmissionIsEqualTo(true)

      viewModel.setFavorite(false)
      assertThatNextEmissionIsEqualTo(false)

      viewModel.setLearnCategories(false)
      assertThatNextEmissionIsEqualTo(true)
    }
  }

  @Test
  fun `Warns about another payee's name, ignoring case and transfers`() = runDatabaseTest { scope ->
    // given
    insertAccount(ACCOUNT, name = "Current")
    val payees = PayeeDao(this)
    payees.insert(TESCO.id, TESCO.name)
    payees.insert(PayeeId("argos"), "Argos")
    payees.insert(PayeeId("transfer"), null, transferAccount = AccountId(ACCOUNT))
    val viewModel = createViewModel(scope, TESCO.id).awaitLoaded()

    viewModel.isDuplicateName.test {
      // its own name is fine
      assertThatNextEmissionIsEqualTo(false)

      viewModel.setName(" argos ")
      assertThatNextEmissionIsEqualTo(true)

      viewModel.setName("Current")
      assertThatNextEmissionIsEqualTo(false)
    }
  }

  @Test
  fun `Can't save a blank name`() = runDatabaseTest { scope ->
    // given
    PayeeDao(this).insert(TESCO.id, TESCO.name)
    val viewModel = createViewModel(scope, TESCO.id).awaitLoaded()

    viewModel.canSave.test {
      assertThatNextEmissionIsEqualTo(true)

      viewModel.setName("  ")
      assertThatNextEmissionIsEqualTo(false)
    }
  }

  @Test
  fun `Can't save a transfer payee`() = runDatabaseTest { scope ->
    // given
    insertAccount(ACCOUNT, name = "Current")
    PayeeDao(this).insert(PayeeId("transfer"), null, transferAccount = AccountId(ACCOUNT))
    val viewModel = createViewModel(scope, PayeeId("transfer")).awaitLoaded()

    viewModel.canSave.test { assertThatNextEmissionIsEqualTo(false) }
  }

  @Test
  fun `Saving renames, favourites and stops learning for the payee, then finishes`() =
    runDatabaseTest { scope ->
      // given
      val payees = PayeeDao(this)
      payees.insert(TESCO.id, TESCO.name)
      val sync = TestSyncController()
      val viewModel = createViewModel(scope, TESCO.id, sync).awaitLoaded()

      // when
      viewModel.setName(" Tesco Extra ")
      viewModel.setFavorite(true)
      viewModel.setLearnCategories(false)
      viewModel.events.test {
        viewModel.save()
        assertThatNextEmissionIsEqualTo(FinishedSaving)
      }

      // then
      assertThat(payees[TESCO.id]).isNotNull().all {
        prop(Payees::name).isEqualTo("Tesco Extra")
        prop(Payees::favorite).isEqualTo(true)
        prop(Payees::learn_categories).isEqualTo(0L)
      }
      assertThat(sync.changes.map { it.column to it.value })
        .containsExactly(
          "name" to MessageValue.String("Tesco Extra"),
          "favorite" to MessageValue.Number(1),
          "learn_categories" to MessageValue.Number(0),
        )
      assertThat(sync.changes.map(LocalChange::row).distinct()).containsExactly(TESCO.id.value)
    }

  private suspend fun EditPayeeViewModel.awaitLoaded(): EditPayeeViewModel = apply {
    state.test {
      do {
        val item = awaitItem()
      } while (item !is Editing)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun BudgetDatabase.createViewModel(
    scope: TestScope,
    id: PayeeId,
    sync: TestSyncController = TestSyncController(),
  ): EditPayeeViewModel {
    Dispatchers.setMain(scope.standardDispatcher)
    val payees = PayeeDao(this)
    return EditPayeeViewModel(
      payeeId = id,
      loader = PayeesLoader(payees, PayeeRuleCounter(RulesDao(this), RuleContextDao(this))),
      payeeDao = payees,
      syncController = sync,
      learning =
        CategoryLearningPref(
          PreferencesDao(this, TestCoroutineContexts(scope.standardDispatcher)),
          sync,
        ),
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

  private companion object {
    const val ACCOUNT = "account"

    val TESCO =
      PayeeItem(
        id = PayeeId("tesco"),
        name = "Tesco",
        isTransfer = false,
        isFavorite = false,
        learnsCategories = true,
        numTransactions = 0,
        numRules = 0,
        isUnused = true,
      )
  }
}
