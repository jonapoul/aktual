package aktual.budget.accounts.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.DatabaseTables.TRANSACTIONS
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.localChange
import aktual.budget.transactions.domain.TransactionWriter
import aktual.test.TestCalendar
import aktual.test.TestSyncController
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlin.test.AfterTest
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CreateAccountViewModelTest {
  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Can't create without a name`() = runDatabaseTest { scope ->
    val viewModel = createViewModel(scope)

    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(CreateAccountState())

      viewModel.setName("Savings")
      assertThatNextEmissionIsEqualTo(CreateAccountState(canCreate = true))

      viewModel.setName("  ")
      assertThatNextEmissionIsEqualTo(CreateAccountState())
    }
  }

  @Test
  fun `A name another account has is a duplicate, even a closed one`() = runDatabaseTest { scope ->
    insertAccount("a", name = "Savings")
    insertAccount("b", name = "Old card")
    accountsQueries.withoutResult { closeAccount(AccountId("b")) }
    val viewModel = createViewModel(scope)

    viewModel.state.test {
      skipItems(1)

      viewModel.setName(" Savings ")
      assertThatNextEmissionIsEqualTo(CreateAccountState(duplicateName = "Savings"))

      viewModel.setName("Old card")
      assertThatNextEmissionIsEqualTo(CreateAccountState(duplicateName = "Old card"))

      viewModel.setName("savings")
      assertThatNextEmissionIsEqualTo(CreateAccountState(canCreate = true))
    }
  }

  @Test
  fun `Can't create with a balance that isn't a number`() = runDatabaseTest { scope ->
    val viewModel = createViewModel(scope)
    viewModel.setName("Savings")

    viewModel.state.test {
      skipItems(1)

      viewModel.setBalance("12a")
      assertThatNextEmissionIsEqualTo(CreateAccountState(isBalanceValid = false))

      viewModel.setBalance("100 + 20")
      assertThatNextEmissionIsEqualTo(CreateAccountState(canCreate = true))
    }
  }

  @Test
  fun `Creating sends the account with its starting balance`() = runDatabaseTest { scope ->
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, sync)
    viewModel.setName(" Credit card ")
    viewModel.setBalance("120.50")
    viewModel.setNegative(true)

    viewModel.events.test {
      viewModel.create()
      assertThatNextEmissionIsEqualTo(CreateAccountEvent.Created(AccountId("id-1")))
    }

    assertThat(sync.changes).all {
      contains(localChange(ACCOUNTS, "id-1", "name", "Credit card"))
      contains(localChange(ACCOUNTS, "id-1", "offbudget", false))
    }
    assertThat(sync.changes.values(TRANSACTIONS, "amount"))
      .containsExactly(MessageValue.Number(-12050))
  }

  @Test
  fun `Creating off budget with no balance adds no transaction`() = runDatabaseTest { scope ->
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, sync)
    viewModel.setName("House")
    viewModel.setOffBudget(true)

    viewModel.events.test {
      viewModel.create()
      assertThatNextEmissionIsEqualTo(CreateAccountEvent.Created(AccountId("id-1")))
    }

    assertThat(sync.changes).contains(localChange(ACCOUNTS, "id-1", "offbudget", true))
    assertThat(sync.changes.values(TRANSACTIONS, "amount")).isEmpty()
  }

  @Test
  fun `A failed create shows its error until dismissed`() = runDatabaseTest { scope ->
    val viewModel = createViewModel(scope, FailingSyncController)

    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(CreateAccountState())
      viewModel.setName("Savings")
      assertThatNextEmissionIsEqualTo(CreateAccountState(canCreate = true))

      viewModel.create()
      assertThatNextEmissionIsEqualTo(CreateAccountState(canCreate = true, saveError = "Offline"))

      viewModel.dismissSaveError()
      assertThatNextEmissionIsEqualTo(CreateAccountState(canCreate = true))
    }
  }

  private fun BudgetDatabase.createViewModel(
    scope: TestScope,
    sync: BudgetSyncController = TestSyncController(),
  ): CreateAccountViewModel {
    Dispatchers.setMain(StandardTestDispatcher(scope.testScheduler))
    val accountDao = AccountDao(this)
    var nextId = 1
    return CreateAccountViewModel(
      writer =
        TransactionWriter(
          syncController = sync,
          accountDao = accountDao,
          categoryDao = CategoryDao(this),
          payeeDao = PayeeDao(this),
          transactionDao = TransactionDao(this),
          uuidGenerator = { "id-${nextId++}" },
          clock = Clock.System,
        ),
      accountDao = accountDao,
      calendar = TestCalendar(LocalDate(2026, 10, 24)),
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

  private fun List<LocalChange>.values(dataset: String, column: String): List<MessageValue> =
    filter {
      it.dataset == dataset && it.column == column
    }
    .map { it.value }

  private object FailingSyncController : BudgetSyncController {
    override suspend fun syncChanges(changes: List<LocalChange>) = error("Offline")

    override fun schedule() = Unit
  }
}
