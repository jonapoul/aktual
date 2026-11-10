package aktual.budget.accounts.vm

import aktual.api.client.BankSyncApi
import aktual.api.model.banksync.BankSyncTransactionsRequest
import aktual.api.model.banksync.SimpleFinBatchRequest
import aktual.budget.BudgetSyncController
import aktual.budget.accounts.domain.AccountWriter
import aktual.budget.banksync.domain.AccountDeleter
import aktual.budget.banksync.domain.BankAccountLinker
import aktual.budget.banksync.domain.BankSyncController
import aktual.budget.banksync.domain.BankSyncProgress
import aktual.budget.banksync.domain.BankSyncResult
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.AccountGroupDao
import aktual.budget.db.dao.BankSyncDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.NotesDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.SyncDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Amount
import aktual.budget.model.Amount.Companion.Zero
import aktual.budget.model.BudgetId
import aktual.budget.model.LocalChange
import aktual.budget.model.localChange
import aktual.budget.transactions.domain.NewTransaction
import aktual.budget.transactions.domain.TransactionWriter
import aktual.di.BudgetCoroutineScope
import aktual.test.TestCalendar
import aktual.test.inMemoryDriverFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

// Changes sent to the sync controller are applied to the database, as the real one does
internal class AccountsTestScope(
  val scope: TestScope,
  val database: BudgetDatabase,
  private val syncDao: SyncDao,
) : BudgetSyncController {
  val accountDao = AccountDao(database)
  val groupDao = AccountGroupDao(database)
  val categoryDao = CategoryDao(database)
  val transactionDao = TransactionDao(database)
  private val payeeDao = PayeeDao(database)
  private var nextId = 1
  private val viewModels = mutableListOf<ViewModel>()

  val transactions =
    TransactionWriter(
      syncController = this,
      accountDao = accountDao,
      categoryDao = categoryDao,
      payeeDao = payeeDao,
      transactionDao = transactionDao,
      categoryLearner = {},
      uuidGenerator = ::uuid,
      clock = Clock.System,
    )

  val writer =
    AccountWriter(
      writer = transactions,
      syncController = this,
      accountDao = accountDao,
      groupDao = groupDao,
      payeeDao = payeeDao,
      notesDao = NotesDao(database),
      uuidGenerator = ::uuid,
      calendar = TestCalendar(TODAY),
    )

  val linker =
    BankAccountLinker(
      api = UnusedBankSyncApi,
      accountDao = accountDao,
      dao = BankSyncDao(database),
      writer = transactions,
      syncController = this,
      bankSync = UnusedBankSyncController,
      uuidGenerator = ::uuid,
      scope = BudgetCoroutineScope(scope),
    )

  val deleter = AccountDeleter(linker, transactions)

  // Cancelled when the test ends, since their scopes aren't children of the test's
  fun <T : ViewModel> track(viewModel: T): T = viewModel.also(viewModels::add)

  suspend fun createAccount(
    name: String,
    offBudget: Boolean = false,
    balance: Amount = Zero,
    transactions: Int = 1,
  ): AccountId {
    val id = this.transactions.write { createAccount(name, offBudget, Zero, TODAY) }
    repeat(transactions) { i ->
      val amount = if (i == 0) balance else Zero
      this.transactions.write {
        insert(NewTransaction(account = id, date = TODAY, amount = amount))
      }
    }
    return id
  }

  // Linked through SimpleFIN, so unlinking never calls the bank sync API
  suspend fun link(id: AccountId) =
    syncChanges(
      listOf(
        localChange(ACCOUNTS, id.value, "account_id", "ACT-1"),
        localChange(ACCOUNTS, id.value, "bank", uuid()),
        localChange(ACCOUNTS, id.value, "account_sync_source", AccountSyncSource.SimpleFin.value),
      ),
    )

  fun close() = viewModels.forEach { it.viewModelScope.cancel() }

  override suspend fun syncChanges(changes: List<LocalChange>) {
    syncDao.sendMessages(changes)
  }

  override fun schedule() = Unit

  private fun uuid() = "00000000-0000-0000-0000-${nextId++.toString().padStart(12, '0')}"

  companion object {
    val TODAY = LocalDate(2026, 11, 10)
  }
}

internal fun runAccountsTest(action: suspend AccountsTestScope.() -> Unit) = runTest {
  val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
  driver.use {
    val database = buildDatabase(driver)
    val testScope = AccountsTestScope(this, database, SyncDao(database, driver, Clock.System))
    try {
      testScope.action()
    } finally {
      testScope.close()
    }
  }
}

private object UnusedBankSyncApi : BankSyncApi {
  override suspend fun status(source: AccountSyncSource) = error("Not used")

  override suspend fun transactions(
    source: AccountSyncSource,
    request: BankSyncTransactionsRequest,
  ) = error("Not used")

  override suspend fun simpleFinBatch(request: SimpleFinBatchRequest) = error("Not used")

  override suspend fun accounts(source: AccountSyncSource) = error("Not used")

  override suspend fun goCardlessBanks(country: String, showDemo: Boolean) = error("Not used")

  override suspend fun goCardlessLogin(bankId: String) = error("Not used")

  override suspend fun goCardlessAccounts(requisitionId: String) = error("Not used")

  override suspend fun removeGoCardlessRequisition(requisitionId: String) = error("Not used")
}

private object UnusedBankSyncController : BankSyncController {
  override val progress = MutableStateFlow(BankSyncProgress())
  override val finished = MutableSharedFlow<List<BankSyncResult>>()

  override fun start(accounts: Set<AccountId>) = error("Not used")

  override suspend fun sync(accounts: Set<AccountId>) = error("Not used")
}
