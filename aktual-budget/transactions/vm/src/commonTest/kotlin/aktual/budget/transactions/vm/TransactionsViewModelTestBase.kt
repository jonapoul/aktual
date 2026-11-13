package aktual.budget.transactions.vm

import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.SyncDao
import aktual.budget.db.dao.TagsDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSpec
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionsSpec
import aktual.budget.transactions.vm.test.BUDGET_ID
import aktual.budget.transactions.vm.test.FakeBankSyncController
import aktual.budget.transactions.vm.test.FakeNewTransactions
import aktual.budget.transactions.vm.test.LINKED
import aktual.budget.transactions.vm.test.METADATA
import aktual.budget.transactions.vm.test.TOKEN
import aktual.budget.transactions.vm.test.insertAccount
import aktual.budget.transactions.vm.test.insertCategory
import aktual.budget.transactions.vm.test.insertPayee
import aktual.di.AppGraph
import aktual.di.AppScope
import aktual.di.RunLevelController
import aktual.di.RunLevelState
import aktual.test.TestAppDirectoryContainer
import aktual.test.TestBudgetFilesContainer
import aktual.test.TestCoroutineContainer
import alakazam.kotlin.CoroutineContexts
import alakazam.test.TestCoroutineContexts
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createDynamicGraph
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath

@Suppress("AbstractClassCanBeConcreteClass")
internal abstract class TransactionsViewModelTestBase {
  // real
  protected lateinit var viewModel: TransactionsViewModel
  protected lateinit var contexts: CoroutineContexts
  protected lateinit var accounts: AccountDao
  protected lateinit var transactions: TransactionDao
  protected lateinit var payees: PayeeDao
  protected lateinit var categories: CategoryDao
  protected lateinit var tags: TagsDao
  protected lateinit var sync: SyncDao
  protected lateinit var factory: TransactionsViewModel.Factory

  // fake
  protected lateinit var bankSync: FakeBankSyncController
  protected lateinit var newTransactions: FakeNewTransactions

  private lateinit var appGraph: TestAppGraph
  private lateinit var rootDir: Path

  @AfterTest
  fun after() {
    // viewModelScope holds SQLDelight Flow subscriptions; cancel it so connections are released
    // before we close the driver
    if (::viewModel.isInitialized) viewModel.viewModelScope.cancel()
    appGraph.close()
    FileSystem.SYSTEM.deleteRecursively(rootDir)
  }

  // The view model's writes finish off the test scheduler, so this waits in real time
  @Suppress("InjectDispatcher") // Real time, off the test scheduler
  protected suspend fun TestScope.awaitUntil(condition: suspend () -> Boolean) {
    repeat(times = 500) {
      advanceUntilIdle()
      if (condition()) return
      withContext(Dispatchers.Default) { delay(10.milliseconds) }
    }
    error("Timed out waiting")
  }

  protected suspend fun TestScope.buildViewModel(spec: AccountSpec) {
    rootDir = createTempDirectory().toOkioPath()
    contexts = TestCoroutineContexts(StandardTestDispatcher(testScheduler))
    appGraph =
      createDynamicGraph<TestAppGraph>(
        TestCoroutineContainer(backgroundScope, contexts),
        TestBudgetFilesContainer(rootDir),
        TestAppDirectoryContainer(rootDir),
      )

    with(appGraph.runLevelController) {
      init(listOf(appGraph))
      onServerChosen(Demo)
      onLoggedIn(TOKEN)
      val budgetGraph = onBudget(BUDGET_ID, METADATA)
      accounts = budgetGraph[AccountDao::class]
      transactions = budgetGraph[TransactionDao::class]
      payees = budgetGraph[PayeeDao::class]
      categories = budgetGraph[CategoryDao::class]
      tags = budgetGraph[TagsDao::class]
      sync = budgetGraph[SyncDao::class]
      bankSync = budgetGraph[FakeBankSyncController::class]
      newTransactions = budgetGraph[FakeNewTransactions::class]
    }

    // add some utility entities
    accounts.insertAccount(AccountId("a"), "Amex")
    accounts.insertAccount(AccountId("b"), "Barclays")
    accounts.insertAccount(AccountId("c"), "Chase")
    accounts.insert(
      id = LINKED,
      accountId = "remote",
      name = "Linked",
      accountSyncSource = SimpleFin,
    )

    payees.insertPayee(PayeeId("a"), "Argos")
    payees.insertPayee(PayeeId("b"), "B&Q")
    payees.insertPayee(PayeeId("c"), "Co-op")
    payees.insert(PayeeId("t"), name = null, transferAccount = AccountId("b"))

    categories.insertCategory(CategoryId("a"), "Additional")
    categories.insertCategory(CategoryId("b"), "Building")
    categories.insertCategory(CategoryId("c"), "Car")

    val viewModelFactory = appGraph.runLevelState.viewModelFactory().first()
    factory =
      viewModelFactory.createManuallyAssistedFactory(TransactionsViewModel.Factory::class).invoke()
    viewModel = factory.create(TransactionsSpec(spec))
  }

  @DependencyGraph(AppScope::class)
  internal interface TestAppGraph : AppGraph {
    val runLevelController: RunLevelController
    val runLevelState: RunLevelState
  }
}
