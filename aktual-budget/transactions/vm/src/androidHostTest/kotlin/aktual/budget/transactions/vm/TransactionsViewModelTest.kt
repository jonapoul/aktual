package aktual.budget.transactions.vm

import aktual.app.di.CoroutineContainer
import aktual.app.di.GithubApiContainer
import aktual.budget.db.dao.TransactionsDao
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSpec
import aktual.budget.model.AccountSpec.AllAccounts
import aktual.budget.model.AccountSpec.SpecificAccount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsSpec
import aktual.core.di.AppGraph
import aktual.core.di.BudgetGraph
import aktual.core.di.BudgetGraphHolder
import alakazam.kotlin.core.CoroutineContexts
import alakazam.test.core.TestCoroutineContexts
import android.content.Context
import androidx.paging.PagingSource
import androidx.test.core.app.ApplicationProvider
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
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
    val pagingSource = TransactionsPagingSource(transactionsDao, accountId = null)

    // when
    val result = pagingSource.load(
      PagingSource.LoadParams.Refresh(
        key = null,
        loadSize = 50,
        placeholdersEnabled = false,
      ),
    )

    // then
    assertThat(result).isInstanceOf<PagingSource.LoadResult.Page<Int, TransactionId>>()
    val page = result as PagingSource.LoadResult.Page
    assertThat(page.data).isEmpty()
    assertThat(page.prevKey).isNull()
    assertThat(page.nextKey).isNull()
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
    val pagingSource = TransactionsPagingSource(transactionsDao, accountId = null)

    // when
    val result = pagingSource.load(
      PagingSource.LoadParams.Refresh(
        key = null,
        loadSize = 50,
        placeholdersEnabled = false,
      ),
    )

    // then
    assertThat(result).isInstanceOf<PagingSource.LoadResult.Page<Int, TransactionId>>()
    val page = result as PagingSource.LoadResult.Page
    assertThat(page.data).containsExactly(ID_C, ID_B, ID_A) // Returned in reverse insertion order
    assertThat(page.prevKey).isNull()
    assertThat(page.nextKey).isEqualTo(1) // PagingSource always returns nextKey unless data is empty
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
    val pagingSource = TransactionsPagingSource(transactionsDao, accountId = AccountId("a"))

    // when
    val result = pagingSource.load(
      PagingSource.LoadParams.Refresh(
        key = null,
        loadSize = 50,
        placeholdersEnabled = false,
      ),
    )

    // then
    assertThat(result).isInstanceOf<PagingSource.LoadResult.Page<Int, TransactionId>>()
    val page = result as PagingSource.LoadResult.Page
    assertThat(page.data).containsExactly(ID_A)
    assertThat(page.prevKey).isNull()
    assertThat(page.nextKey).isEqualTo(1) // PagingSource always returns nextKey unless data is empty
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
    val pagingSource = TransactionsPagingSource(transactionsDao, accountId = null)

    // when
    val result = pagingSource.load(
      PagingSource.LoadParams.Refresh(
        key = null,
        loadSize = 50,
        placeholdersEnabled = false,
      ),
    )

    // then
    assertThat(result).isInstanceOf<PagingSource.LoadResult.Page<Int, TransactionId>>()
    val page = result as PagingSource.LoadResult.Page
    assertThat(page.data).containsExactly(ID_F, ID_E, ID_D, ID_C, ID_B, ID_A) // Returned in reverse order
    assertThat(page.prevKey).isNull()
    assertThat(page.nextKey).isEqualTo(1) // PagingSource always returns nextKey unless data is empty
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
    val pagingSource = TransactionsPagingSource(transactionsDao, accountId = null)

    // when - load first page with size 2
    val firstPage = pagingSource.load(
      PagingSource.LoadParams.Refresh(
        key = null,
        loadSize = 2,
        placeholdersEnabled = false,
      ),
    ) as PagingSource.LoadResult.Page

    // then - first page contains first 2 items (in reverse order)
    assertThat(firstPage.data).containsExactly(ID_F, ID_E)
    assertThat(firstPage.prevKey).isNull()
    assertThat(firstPage.nextKey).isEqualTo(1)

    // when - load second page
    val secondPage = pagingSource.load(
      PagingSource.LoadParams.Append(
        key = 1,
        loadSize = 2,
        placeholdersEnabled = false,
      ),
    ) as PagingSource.LoadResult.Page

    // then - second page contains next 2 items
    assertThat(secondPage.data).containsExactly(ID_D, ID_C)
    assertThat(secondPage.prevKey).isEqualTo(0)
    assertThat(secondPage.nextKey).isEqualTo(2)

    // when - load third page
    val thirdPage = pagingSource.load(
      PagingSource.LoadParams.Append(
        key = 2,
        loadSize = 2,
        placeholdersEnabled = false,
      ),
    ) as PagingSource.LoadResult.Page

    // then - third page contains remaining items
    assertThat(thirdPage.data).containsExactly(ID_B, ID_A)
    assertThat(thirdPage.prevKey).isEqualTo(1)
    assertThat(thirdPage.nextKey).isEqualTo(3) // Has next key because data is not empty
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
