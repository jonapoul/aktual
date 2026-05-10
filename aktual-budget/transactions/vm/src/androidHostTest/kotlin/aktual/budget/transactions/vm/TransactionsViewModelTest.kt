package aktual.budget.transactions.vm

import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSpec
import aktual.budget.model.AccountSpec.AllAccounts
import aktual.budget.model.AccountSpec.SpecificAccount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionsSpec
import aktual.core.model.ServerUrl
import aktual.di.AppGraph
import aktual.di.AppScope
import aktual.di.RunLevelController
import aktual.di.RunLevelState
import aktual.test.TestContainer
import aktual.test.coroutineContainer
import alakazam.kotlin.CoroutineContexts
import alakazam.test.TestCoroutineContexts
import android.os.Looper
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingSource.LoadParams
import assertk.assertThat
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createDynamicGraph
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class TransactionsViewModelTest {
  // real
  private lateinit var viewModel: TransactionsViewModel
  private lateinit var contexts: CoroutineContexts
  private lateinit var accounts: AccountDao
  private lateinit var transactions: TransactionDao
  private lateinit var payees: PayeeDao
  private lateinit var categories: CategoryDao

  // fake
  private lateinit var appGraph: TestAppGraph
  private lateinit var rootDir: Path

  @AfterTest
  fun after() {
    // viewModelScope holds SQLDelight Flow subscriptions; cancel it and flush the main looper
    // so connections are released before we close the driver
    if (::viewModel.isInitialized) viewModel.viewModelScope.cancel()
    Shadows.shadowOf(Looper.getMainLooper()).idle()
    appGraph.runLevelController.close()
    FileSystem.SYSTEM.deleteRecursively(rootDir)
  }

  private suspend fun TestScope.buildViewModel(spec: AccountSpec) {
    rootDir = createTempDirectory().toOkioPath()
    contexts = TestCoroutineContexts(StandardTestDispatcher(testScheduler))
    appGraph =
      createDynamicGraph<TestAppGraph>(coroutineContainer(contexts), TestContainer(rootDir))

    with(appGraph.runLevelController) {
      init(listOf(appGraph))
      onServerChosen(ServerUrl.Demo)
      onLoggedIn(TOKEN)
      val budgetGraph = onBudget(METADATA)
      accounts = budgetGraph[AccountDao::class]
      transactions = budgetGraph[TransactionDao::class]
      payees = budgetGraph[PayeeDao::class]
      categories = budgetGraph[CategoryDao::class]
    }

    // add some utility entities
    accounts.insertAccount(AccountId("a"), "Amex")
    accounts.insertAccount(AccountId("b"), "Barclays")
    accounts.insertAccount(AccountId("c"), "Chase")

    payees.insertPayee(PayeeId("a"), "Argos")
    payees.insertPayee(PayeeId("b"), "B&Q")
    payees.insertPayee(PayeeId("c"), "Co-op")

    categories.insertCategory(CategoryId("a"), "Additional")
    categories.insertCategory(CategoryId("b"), "Building")
    categories.insertCategory(CategoryId("c"), "Car")

    val viewModelFactory = appGraph.runLevelState.viewModelFactory().first()
    viewModel =
      viewModelFactory
        .createManuallyAssistedFactory(TransactionsViewModel.Factory::class)
        .invoke()
        .create(TransactionsSpec(spec))
  }

  @Test
  fun `Empty transaction list from all accounts`() = runTest {
    // given
    buildViewModel(AllAccounts)
    val source = TransactionsPagingSource(transactions, spec = AllAccounts)

    // when
    val result =
      source.load(LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false))

    // then
    assertThat(result).isPage().withData(emptyList()).withPrevKey(null).withNextKey(null)
  }

  @Test
  fun `Transactions from all accounts`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a")
      insertTransaction(id = "b", account = "b", category = "b", payee = "b")
      insertTransaction(id = "c", account = "c", category = "c", payee = "c")
    }
    advanceUntilIdle()

    val source = TransactionsPagingSource(transactions, AllAccounts)

    // when
    val result =
      source.load(LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false))

    // then
    assertThat(result)
      .isPage()
      .withData(ID_C, ID_B, ID_A) // Returned in reverse insertion order
      .withPrevKey(null)
      .withNextKey(null) // No more pages since we loaded fewer items than page size
  }

  @Test
  fun `Transactions from one account`() = runTest {
    // given
    buildViewModel(SpecificAccount(AccountId("a")))
    with(transactions) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a") // included
      insertTransaction(id = "b", account = "b", category = "b", payee = "b") // ignored
      insertTransaction(id = "c", account = "c", category = "c", payee = "c") // ignored
    }
    advanceUntilIdle()

    val source =
      TransactionsPagingSource(dao = transactions, spec = SpecificAccount(AccountId("a")))

    // when
    val result =
      source.load(LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false))

    // then
    assertThat(result)
      .isPage()
      .withData(ID_A)
      .withPrevKey(null)
      .withNextKey(null) // No more pages since we loaded fewer items than page size
  }

  @Test
  fun `Multiple transactions with different dates`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a", date = DATE_1)
      insertTransaction(id = "b", account = "b", category = "b", payee = "b", date = DATE_1)
      insertTransaction(id = "c", account = "c", category = "c", payee = "c", date = DATE_1)
      insertTransaction(id = "d", account = "c", category = "c", payee = "c", date = DATE_2)
      insertTransaction(id = "e", account = "c", category = "c", payee = "c", date = DATE_2)
      insertTransaction(id = "f", account = "c", category = "c", payee = "c", date = DATE_3)
    }
    advanceUntilIdle()

    val pagingSource = TransactionsPagingSource(transactions, AllAccounts)

    // when
    val result =
      pagingSource.load(LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false))

    // then
    assertThat(result)
      .isPage()
      .withData(ID_F, ID_E, ID_D, ID_C, ID_B, ID_A)
      .withPrevKey(null)
      .withNextKey(null) // No more pages since we loaded fewer items than page size
  }

  @Test
  fun `Paging loads data in pages`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a", date = DATE_1)
      insertTransaction(id = "b", account = "b", category = "b", payee = "b", date = DATE_1)
      insertTransaction(id = "c", account = "c", category = "c", payee = "c", date = DATE_1)
      insertTransaction(id = "d", account = "c", category = "c", payee = "c", date = DATE_2)
      insertTransaction(id = "e", account = "c", category = "c", payee = "c", date = DATE_2)
      insertTransaction(id = "f", account = "c", category = "c", payee = "c", date = DATE_3)
    }
    advanceUntilIdle()

    val source = TransactionsPagingSource(transactions, AllAccounts)

    // when - load first page with size 2
    val firstPage =
      source.load(LoadParams.Refresh(key = null, loadSize = 2, placeholdersEnabled = false))

    // then - first page contains first 2 items (in reverse order)
    assertThat(firstPage).isPage().withData(ID_F, ID_E).withPrevKey(null).withNextKey(1)

    // when - load second page
    val secondPage =
      source.load(LoadParams.Append(key = 1, loadSize = 2, placeholdersEnabled = false))

    // then - second page contains next 2 items
    assertThat(secondPage).isPage().withData(ID_D, ID_C).withPrevKey(0).withNextKey(2)

    // when - load third page
    val thirdPage =
      source.load(LoadParams.Append(key = 2, loadSize = 2, placeholdersEnabled = false))

    // then - third page contains remaining items
    assertThat(thirdPage)
      .isPage()
      .withData(ID_B, ID_A)
      .withPrevKey(1)
      .withNextKey(3) // More pages possible since we loaded exactly the page size
  }

  @DependencyGraph(AppScope::class)
  internal interface TestAppGraph : AppGraph {
    val runLevelController: RunLevelController
    val runLevelState: RunLevelState
  }
}
