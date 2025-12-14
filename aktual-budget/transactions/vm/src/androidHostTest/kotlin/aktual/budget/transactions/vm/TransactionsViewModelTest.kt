package aktual.budget.transactions.vm

import aktual.app.di.GithubApiContainer
import aktual.budget.db.dao.TransactionsDao
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSpec
import aktual.budget.model.AccountSpec.AllAccounts
import aktual.budget.model.AccountSpec.SpecificAccount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionsSpec
import aktual.core.di.AppGraph
import aktual.core.di.BudgetGraph
import aktual.core.di.BudgetGraphHolder
import aktual.core.di.CoroutineContainer
import alakazam.kotlin.core.CoroutineContexts
import alakazam.test.core.TestCoroutineContexts
import android.content.Context
import androidx.paging.PagingSource.LoadParams
import androidx.test.core.app.ApplicationProvider
import assertk.assertThat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class TransactionsViewModelTest {
  // real
  private lateinit var viewModel: TransactionsViewModel
  private lateinit var budgetGraph: BudgetGraph
  private lateinit var contexts: CoroutineContexts

  // fake
  private lateinit var appGraph: TestAppGraph

  @AfterTest
  fun after() {
    budgetGraph.driver.close()
  }

  private suspend fun TestScope.buildViewModel(spec: AccountSpec) {
    val context = ApplicationProvider.getApplicationContext<Context>()

    contexts = TestCoroutineContexts(StandardTestDispatcher(testScheduler))
    appGraph = createGraphFactory<TestAppGraph.Factory>().create(
      scope = this,
      contexts = contexts,
      context = context,
    )

    budgetGraph = appGraph.budgetGraphHolder.update(METADATA)

    // add some utility entities
    with(budgetGraph.database) {
      insertAccount(AccountId("a"), "Amex")
      insertAccount(AccountId("b"), "Barclays")
      insertAccount(AccountId("c"), "Chase")

      insertPayee(PayeeId("a"), "Argos")
      insertPayee(PayeeId("b"), "B&Q")
      insertPayee(PayeeId("c"), "Co-op")

      insertCategory(CategoryId("a"), "Additional")
      insertCategory(CategoryId("b"), "Building")
      insertCategory(CategoryId("c"), "Car")
    }

    viewModel = appGraph
      .metroViewModelFactory
      .createManuallyAssistedFactory(TransactionsViewModel.Factory::class)
      .invoke()
      .create(TOKEN, BUDGET_ID, TransactionsSpec(spec))
  }

  @Test
  fun `Empty transaction list from all accounts`() = runTest {
    // given
    buildViewModel(AllAccounts)
    val transactionsDao = TransactionsDao(budgetGraph.database, contexts)
    val source = TransactionsPagingSource(transactionsDao, AllAccounts)

    // when
    val result = source.load(LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false))

    // then
    assertThat(result)
      .isPage()
      .withData(emptyList())
      .withPrevKey(null)
      .withNextKey(null)
  }

  @Test
  fun `Transactions from all accounts`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(budgetGraph.database) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a")
      insertTransaction(id = "b", account = "b", category = "b", payee = "b")
      insertTransaction(id = "c", account = "c", category = "c", payee = "c")
    }
    advanceUntilIdle()

    val transactionsDao = TransactionsDao(budgetGraph.database, contexts)
    val source = TransactionsPagingSource(transactionsDao, AllAccounts)

    // when
    val result = source.load(LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false))

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
    with(budgetGraph.database) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a") // included
      insertTransaction(id = "b", account = "b", category = "b", payee = "b") // ignored
      insertTransaction(id = "c", account = "c", category = "c", payee = "c") // ignored
    }
    advanceUntilIdle()

    val transactionsDao = TransactionsDao(budgetGraph.database, contexts)
    val source = TransactionsPagingSource(transactionsDao, SpecificAccount(AccountId("a")))

    // when
    val result = source.load(LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false))

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
    with(budgetGraph.database) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a", date = DATE_1)
      insertTransaction(id = "b", account = "b", category = "b", payee = "b", date = DATE_1)
      insertTransaction(id = "c", account = "c", category = "c", payee = "c", date = DATE_1)
      insertTransaction(id = "d", account = "c", category = "c", payee = "c", date = DATE_2)
      insertTransaction(id = "e", account = "c", category = "c", payee = "c", date = DATE_2)
      insertTransaction(id = "f", account = "c", category = "c", payee = "c", date = DATE_3)
    }
    advanceUntilIdle()

    val transactionsDao = TransactionsDao(budgetGraph.database, contexts)
    val pagingSource = TransactionsPagingSource(transactionsDao, AllAccounts)

    // when
    val result = pagingSource.load(LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false))

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
    with(budgetGraph.database) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a", date = DATE_1)
      insertTransaction(id = "b", account = "b", category = "b", payee = "b", date = DATE_1)
      insertTransaction(id = "c", account = "c", category = "c", payee = "c", date = DATE_1)
      insertTransaction(id = "d", account = "c", category = "c", payee = "c", date = DATE_2)
      insertTransaction(id = "e", account = "c", category = "c", payee = "c", date = DATE_2)
      insertTransaction(id = "f", account = "c", category = "c", payee = "c", date = DATE_3)
    }
    advanceUntilIdle()

    val transactionsDao = TransactionsDao(budgetGraph.database, contexts)
    val source = TransactionsPagingSource(transactionsDao, AllAccounts)

    // when - load first page with size 2
    val firstPage = source.load(LoadParams.Refresh(key = null, loadSize = 2, placeholdersEnabled = false))

    // then - first page contains first 2 items (in reverse order)
    assertThat(firstPage)
      .isPage()
      .withData(ID_F, ID_E)
      .withPrevKey(null)
      .withNextKey(1)

    // when - load second page
    val secondPage = source.load(LoadParams.Append(key = 1, loadSize = 2, placeholdersEnabled = false))

    // then - second page contains next 2 items
    assertThat(secondPage)
      .isPage()
      .withData(ID_D, ID_C)
      .withPrevKey(0)
      .withNextKey(2)

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

  @DependencyGraph(
    scope = AppScope::class,
    excludes = [CoroutineContainer::class, GithubApiContainer::class],
  )
  internal interface TestAppGraph : AppGraph {
    val budgetGraphHolder: BudgetGraphHolder

    @DependencyGraph.Factory
    fun interface Factory {
      fun create(
        @Provides scope: CoroutineScope,
        @Provides contexts: CoroutineContexts,
        @Provides context: Context,
      ): TestAppGraph
    }
  }
}
