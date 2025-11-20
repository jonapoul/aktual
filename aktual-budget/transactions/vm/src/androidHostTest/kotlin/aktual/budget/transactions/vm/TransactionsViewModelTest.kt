/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.transactions.vm

import aktual.app.di.AndroidViewModelGraph
import aktual.app.di.CoroutineContainer
import aktual.app.di.GithubApiContainer
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
import aktual.core.di.assisted
import aktual.test.DummyViewModelContainer
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.kotlin.core.CoroutineContexts
import alakazam.test.core.TestCoroutineContexts
import android.content.Context
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEmpty
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import kotlinx.collections.immutable.persistentListOf
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
class TransactionsViewModelTest : AppGraph.Holder {
  // real
  private lateinit var viewModel: TransactionsViewModel
  private lateinit var budgetGraph: BudgetGraph

  // fake
  private lateinit var appGraph: TestAppGraph

  override fun get(): AppGraph = appGraph

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
      assertThat(awaitItem()).isEmpty()
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
//  fun `Transactions sorting by date`() = runTest {
//    // given
//    buildViewModel(AllAccounts)
//    with(budgetGraph.database) {
//      insertTransaction(id = "a", account = "a", category = "a", payee = "a", date = DATE_1)
//      insertTransaction(id = "b", account = "b", category = "b", payee = "b", date = DATE_1)
//      insertTransaction(id = "c", account = "c", category = "c", payee = "c", date = DATE_1)
//      insertTransaction(id = "d", account = "c", category = "c", payee = "c", date = DATE_2)
//      insertTransaction(id = "e", account = "c", category = "c", payee = "c", date = DATE_2)
//      insertTransaction(id = "f", account = "c", category = "c", payee = "c", date = DATE_3)
//    }
//
//    setSortingDirection(Descending)
//    advanceUntilIdle()
//
//    viewModel.transactions.test {
//      // latest date first
//      assertThatNextEmissionIsEqualTo(
//        persistentListOf(
//          DatedTransactions(DATE_1, persistentListOfIds("a", "b", "c")),
//          DatedTransactions(DATE_2, persistentListOfIds("d", "e")),
//          DatedTransactions(DATE_3, persistentListOfIds("f")),
//        ),
//      )
//
//      // then opposite sorting
//      setSortingDirection(Ascending)
//      assertThatNextEmissionIsEqualTo(
//        persistentListOf(
//          DatedTransactions(DATE_1, persistentListOfIds("a", "b", "c")),
//          DatedTransactions(DATE_2, persistentListOfIds("d", "e")),
//          DatedTransactions(DATE_3, persistentListOfIds("f")),
//        ),
//      )
//
//      cancelAndIgnoreRemainingEvents()
//    }
//  }
//
//  private fun setSortingDirection(
//    direction: SortDirection = Ascending,
//  ) = budgetGraph.localPreferences.update { metadata ->
//    metadata
//      .set(SortColumnKey, Date)
//      .set(SortDirectionKey, direction)
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
