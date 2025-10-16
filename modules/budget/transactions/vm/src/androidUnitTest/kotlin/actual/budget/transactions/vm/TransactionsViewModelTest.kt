package actual.budget.transactions.vm

import actual.app.di.AndroidViewModelGraph
import actual.app.di.CoroutineContainer
import actual.app.di.GithubApiContainer
import actual.budget.model.AccountId
import actual.budget.model.AccountSpec
import actual.budget.model.AccountSpec.AllAccounts
import actual.budget.model.AccountSpec.SpecificAccount
import actual.budget.model.CategoryId
import actual.budget.model.PayeeId
import actual.budget.model.TransactionsSpec
import actual.core.di.AppGraph
import actual.core.di.BudgetGraph
import actual.core.di.BudgetGraphHolder
import actual.core.di.assisted
import actual.test.DummyViewModelContainer
import actual.test.assertThatNextEmissionIsEqualTo
import alakazam.kotlin.core.CoroutineContexts
import alakazam.test.core.TestCoroutineContexts
import android.content.Context
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TransactionsViewModelTest : AppGraph.Holder {
  // real
  private lateinit var viewModel: TransactionsViewModel
  private lateinit var budgetGraph: BudgetGraph

  // fake
  private lateinit var appGraph: TestAppGraph

  override fun invoke(): AppGraph = appGraph

  @AfterTest
  fun after() {
    budgetGraph.driver.close()
  }

  private suspend fun TestScope.buildViewModel(spec: AccountSpec) {
    val context = ApplicationProvider.getApplicationContext<Context>()

    appGraph = createGraphFactory<TestAppGraph.Factory>().create(
      scope = this,
      contexts = TestCoroutineContexts(StandardTestDispatcher(testScheduler)),
      context = context,
      holder = this@TransactionsViewModelTest,
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
      .create(CreationExtras.Empty)
      .assisted<TransactionsViewModel, TransactionsViewModel.Factory> {
        create(TOKEN, BUDGET_ID, TransactionsSpec(spec))
      }
  }

  @Test
  fun `Empty transaction list from all accounts`() = runTest {
    // given
    buildViewModel(AllAccounts)

    // when
    viewModel.transactions.test {
      // then
      assertThat(awaitItem()).isEqualTo(persistentListOf())
      advanceUntilIdle()
      expectNoEvents()
      cancel()
    }
  }

  @Test
  fun `Transactions from all accounts grouped in one date`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(budgetGraph.database) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a")
      insertTransaction(id = "b", account = "b", category = "b", payee = "b")
      insertTransaction(id = "c", account = "c", category = "c", payee = "c")
    }
    advanceUntilIdle()

    // when
    viewModel.transactions.test {
      // then
      assertThatNextEmissionIsEqualTo(
        persistentListOf(
          DatedTransactions(DATE_1, persistentListOf(ID_A, ID_B, ID_C)),
        ),
      )
      expectNoEvents()
      cancel()
    }
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

    // when
    viewModel.transactions.test {
      // then
      assertThatNextEmissionIsEqualTo(
        persistentListOf(DatedTransactions(DATE_1, persistentListOf(ID_A))),
      )
      expectNoEvents()
      cancel()
    }
  }

  @Test
  fun `Transactions grouped by date`() = runTest {
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

    // when
    viewModel.transactions.test {
      // then
      assertThatNextEmissionIsEqualTo(
        persistentListOf(
          DatedTransactions(DATE_1, persistentListOfIds("a", "b", "c")),
          DatedTransactions(DATE_2, persistentListOfIds("d", "e")),
          DatedTransactions(DATE_3, persistentListOfIds("f")),
        ),
      )
      expectNoEvents()
      cancel()
    }
  }

  //  @Test
  //  fun `Observing transaction`() = runTest {
  //    // given
  //    buildViewModel(AllAccounts)
  //    with(database) {
  //      insertTransaction(id = ID_A.toString(), account = "a", category = "a", payee = "a", date = DATE_1)
  //      insertTransaction(id = ID_B.toString(), account = "b", category = "b", payee = "b", date = DATE_2)
  //      insertTransaction(id = ID_C.toString(), account = "c", category = "c", payee = "c", date = DATE_3)
  //    }
  //    advanceUntilIdle()
  //
  //    assertThat(viewModel.observe(ID_A).first()).isEqualTo(TRANSACTION_A)
  //    assertThat(viewModel.observe(ID_B).first()).isEqualTo(TRANSACTION_B)
  //    assertThat(viewModel.observe(ID_C).first()).isEqualTo(TRANSACTION_C)
  //  }

  @DependencyGraph(
    scope = AppScope::class,
    excludes = [CoroutineContainer::class, GithubApiContainer::class],
    bindingContainers = [DummyViewModelContainer::class],
  )
  internal interface TestAppGraph : AppGraph, AndroidViewModelGraph.Factory {
    val budgetGraphHolder: BudgetGraphHolder

    @DependencyGraph.Factory
    fun interface Factory {
      fun create(
        @Provides scope: CoroutineScope,
        @Provides contexts: CoroutineContexts,
        @Provides context: Context,
        @Provides holder: AppGraph.Holder,
      ): TestAppGraph
    }
  }
}
