package actual.budget.transactions.vm

import actual.app.di.AndroidViewModelGraph
import actual.app.di.CoroutineContainer
import actual.app.di.GithubApiContainer
import actual.budget.db.AndroidSqlDriverFactory
import actual.budget.db.BudgetDatabase
import actual.budget.db.buildDatabase
import actual.budget.model.AccountId
import actual.budget.model.AccountSpec
import actual.budget.model.AccountSpec.AllAccounts
import actual.budget.model.AccountSpec.SpecificAccount
import actual.budget.model.AndroidBudgetFiles
import actual.budget.model.BudgetFiles
import actual.budget.model.CategoryId
import actual.budget.model.PayeeId
import actual.budget.model.TransactionsSpec
import actual.core.di.AppGraph
import actual.core.di.BudgetGraphHolder
import actual.core.di.assisted
import actual.test.DummyViewModelContainer
import alakazam.kotlin.core.CoroutineContexts
import alakazam.test.core.TestCoroutineContexts
import android.content.Context
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.test.core.app.ApplicationProvider
import app.cash.sqldelight.db.SqlDriver
import app.cash.turbine.test
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
import okio.FileSystem
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TransactionsViewModelTest : AppGraph.Holder {
  // real
  private lateinit var viewModel: TransactionsViewModel
  private lateinit var fileSystem: FileSystem
  private lateinit var driver: SqlDriver

  // fake
  private lateinit var appGraph: TestAppGraph
  private lateinit var files: BudgetFiles
  private lateinit var database: BudgetDatabase

  override fun invoke(): AppGraph = appGraph

  @AfterTest
  fun after() {
    driver.close()
  }

  private suspend fun TestScope.buildViewModel(spec: AccountSpec) {
    val context = ApplicationProvider.getApplicationContext<Context>()
    fileSystem = FileSystem.SYSTEM
    files = AndroidBudgetFiles(context, fileSystem)

    val factory = AndroidSqlDriverFactory(BUDGET_ID, context, files)
    driver = factory.create()
    database = buildDatabase(driver)

    // add some utility entities
    with(database) {
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

    appGraph = createGraphFactory<TestAppGraph.Factory>().create(
      scope = this,
      contexts = TestCoroutineContexts(StandardTestDispatcher(testScheduler)),
      context = context,
      holder = this@TransactionsViewModelTest,
    )

    appGraph.budgetGraphHolder.update(METADATA)

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
      assertEquals(expected = persistentListOf(), actual = awaitItem())
      advanceUntilIdle()
      expectNoEvents()
      cancel()
    }
  }

  @Test
  fun `Transactions from all accounts grouped in one date`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(database) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a")
      insertTransaction(id = "b", account = "b", category = "b", payee = "b")
      insertTransaction(id = "c", account = "c", category = "c", payee = "c")
    }
    advanceUntilIdle()

    // when
    viewModel.transactions.test {
      // then
      assertEquals(
        actual = awaitItem(),
        expected = persistentListOf(
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
    with(database) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a") // included
      insertTransaction(id = "b", account = "b", category = "b", payee = "b") // ignored
      insertTransaction(id = "c", account = "c", category = "c", payee = "c") // ignored
    }
    advanceUntilIdle()

    // when
    viewModel.transactions.test {
      // then
      assertEquals(
        actual = awaitItem(),
        expected = persistentListOf(DatedTransactions(DATE_1, persistentListOf(ID_A))),
      )
      expectNoEvents()
      cancel()
    }
  }

  @Test
  fun `Transactions grouped by date`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(database) {
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
      assertEquals(
        actual = awaitItem(),
        expected = persistentListOf(
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
//    assertEquals(actual = viewModel.observe(ID_A).first(), expected = TRANSACTION_A)
//    assertEquals(actual = viewModel.observe(ID_B).first(), expected = TRANSACTION_B)
//    assertEquals(actual = viewModel.observe(ID_C).first(), expected = TRANSACTION_C)
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
