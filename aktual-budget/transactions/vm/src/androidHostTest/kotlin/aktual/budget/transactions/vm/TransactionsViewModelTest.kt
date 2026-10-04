package aktual.budget.transactions.vm

import aktual.budget.banksync.domain.BankSyncError
import aktual.budget.banksync.domain.BankSyncResult
import aktual.budget.banksync.domain.BankSyncSummary
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.SyncDao
import aktual.budget.db.dao.TagsDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSpec
import aktual.budget.model.AccountSpec.AllAccounts
import aktual.budget.model.AccountSpec.SpecificAccount
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.CategoryId
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.PayeeId
import aktual.budget.model.TagId
import aktual.budget.model.TagSpec
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsSpec
import aktual.core.model.ServerUrl
import aktual.di.AppGraph
import aktual.di.AppScope
import aktual.di.RunLevelController
import aktual.di.RunLevelState
import aktual.test.TestAppDirectoryContainer
import aktual.test.TestBudgetFilesContainer
import aktual.test.TestCoroutineContainer
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.kotlin.CoroutineContexts
import alakazam.test.TestCoroutineContexts
import android.os.Looper
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingSource.LoadParams
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createDynamicGraph
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
  private lateinit var tags: TagsDao
  private lateinit var sync: SyncDao
  private lateinit var bankSync: FakeBankSyncController
  private lateinit var factory: TransactionsViewModel.Factory

  // fake
  private lateinit var appGraph: TestAppGraph
  private lateinit var rootDir: Path

  @AfterTest
  fun after() {
    // viewModelScope holds SQLDelight Flow subscriptions; cancel it and flush the main looper
    // so connections are released before we close the driver
    if (::viewModel.isInitialized) viewModel.viewModelScope.cancel()
    Shadows.shadowOf(Looper.getMainLooper()).idle()
    appGraph.close()
    Dispatchers.resetMain()
    FileSystem.SYSTEM.deleteRecursively(rootDir)
  }

  private suspend fun TestScope.buildViewModel(spec: AccountSpec) {
    rootDir = createTempDirectory().toOkioPath()
    contexts = TestCoroutineContexts(StandardTestDispatcher(testScheduler))
    Dispatchers.setMain(StandardTestDispatcher(testScheduler))
    appGraph =
      createDynamicGraph<TestAppGraph>(
        TestCoroutineContainer(backgroundScope, contexts),
        TestBudgetFilesContainer(rootDir),
        TestAppDirectoryContainer(rootDir),
      )

    with(appGraph.runLevelController) {
      init(listOf(appGraph))
      onServerChosen(ServerUrl.Demo)
      onLoggedIn(TOKEN)
      val budgetGraph = onBudget(BUDGET_ID, METADATA)
      accounts = budgetGraph[AccountDao::class]
      transactions = budgetGraph[TransactionDao::class]
      payees = budgetGraph[PayeeDao::class]
      categories = budgetGraph[CategoryDao::class]
      tags = budgetGraph[TagsDao::class]
      sync = budgetGraph[SyncDao::class]
      bankSync = budgetGraph[FakeBankSyncController::class]
    }

    // add some utility entities
    accounts.insertAccount(AccountId("a"), "Amex")
    accounts.insertAccount(AccountId("b"), "Barclays")
    accounts.insertAccount(AccountId("c"), "Chase")
    accounts.insert(
      id = LINKED,
      accountId = "remote",
      name = "Linked",
      accountSyncSource = AccountSyncSource.SimpleFin,
    )

    payees.insertPayee(PayeeId("a"), "Argos")
    payees.insertPayee(PayeeId("b"), "B&Q")
    payees.insertPayee(PayeeId("c"), "Co-op")

    categories.insertCategory(CategoryId("a"), "Additional")
    categories.insertCategory(CategoryId("b"), "Building")
    categories.insertCategory(CategoryId("c"), "Car")

    val viewModelFactory = appGraph.runLevelState.viewModelFactory().first()
    factory =
      viewModelFactory.createManuallyAssistedFactory(TransactionsViewModel.Factory::class).invoke()
    viewModel = factory.create(TransactionsSpec(spec))
  }

  @Test
  fun `Empty transaction list from all accounts`() = runTest {
    // given
    buildViewModel(AllAccounts)
    val source =
      TransactionsPagingSource(
        transactionDao = transactions,
        tagsDao = tags,
        spec = TransactionsSpec(AllAccounts),
      )

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

    val source =
      TransactionsPagingSource(
        transactionDao = transactions,
        tagsDao = tags,
        spec = TransactionsSpec(AllAccounts),
      )

    // when
    val result =
      source.load(LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false))

    // then
    assertThat(result)
      .isPage()
      .withData(TRANSACTION_C, TRANSACTION_B, TRANSACTION_A) // Returned in reverse insertion order
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
      TransactionsPagingSource(
        transactionDao = transactions,
        tagsDao = tags,
        spec = TransactionsSpec(SpecificAccount(AccountId("a"))),
      )

    // when
    val result =
      source.load(LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false))

    // then
    assertThat(result)
      .isPage()
      .withData(TRANSACTION_A)
      .withPrevKey(null)
      .withNextKey(null) // No more pages since we loaded fewer items than page size
  }

  @Test
  fun `Transactions for a specific tag`() = runTest {
    // given
    buildViewModel(AllAccounts)
    tags.insert(id = TagId("food"), tag = "food", color = null, description = null)
    with(transactions) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a", notes = "lunch #food")
      insertTransaction(id = "b", account = "b", category = "b", payee = "b", notes = "no tag here")
      insertTransaction(id = "c", account = "c", category = "c", payee = "c", notes = "#foodie")
      insertTransaction(id = "d", account = "c", category = "c", payee = "c", notes = "#FOOD again")
    }
    advanceUntilIdle()

    val source =
      TransactionsPagingSource(
        transactionDao = transactions,
        tagsDao = tags,
        spec = TransactionsSpec(tagSpec = TagSpec.SpecificTag(TagId("food"))),
      )

    // when
    val result =
      source.load(LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false))

    // then - only the two exact, case-insensitive #food matches; #foodie is excluded
    assertThat(result)
      .isPage()
      .withData(
        transaction(id = "d", account = "c", category = "c", payee = "c", notes = "#FOOD again"),
        transaction(id = "a", account = "a", category = "a", payee = "a", notes = "lunch #food"),
      )
      .withPrevKey(expected = null)
      .withNextKey(expected = null)
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

    val pagingSource =
      TransactionsPagingSource(
        transactionDao = transactions,
        tagsDao = tags,
        spec = TransactionsSpec(AllAccounts),
      )

    // when
    val result =
      pagingSource.load(LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false))

    // then
    assertThat(result)
      .isPage()
      .withData(DATED_F, DATED_E, DATED_D, DATED_C, DATED_B, DATED_A)
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

    val source =
      TransactionsPagingSource(
        transactionDao = transactions,
        tagsDao = tags,
        spec = TransactionsSpec(AllAccounts),
      )

    // when - load first page with size 2
    val firstPage =
      source.load(LoadParams.Refresh(key = null, loadSize = 2, placeholdersEnabled = false))

    // then - first page contains first 2 items (in reverse order)
    assertThat(firstPage).isPage().withData(DATED_F, DATED_E).withPrevKey(null).withNextKey(1)

    // when - load second page
    val secondPage =
      source.load(LoadParams.Append(key = 1, loadSize = 2, placeholdersEnabled = false))

    // then - second page contains next 2 items
    assertThat(secondPage).isPage().withData(DATED_D, DATED_C).withPrevKey(0).withNextKey(2)

    // when - load third page
    val thirdPage =
      source.load(LoadParams.Append(key = 2, loadSize = 2, placeholdersEnabled = false))

    // then - third page contains remaining items
    assertThat(thirdPage)
      .isPage()
      .withData(DATED_B, DATED_A)
      .withPrevKey(1)
      .withNextKey(3) // More pages possible since we loaded exactly the page size
  }

  @Test
  fun `Tag-filtered transactions are paged`() = runTest {
    // given
    buildViewModel(AllAccounts)
    tags.insert(id = TagId("food"), tag = "food", color = null, description = null)
    with(transactions) {
      insertTransaction("a", "a", "a", "a", notes = "#food 1", date = DATE_1)
      insertTransaction("b", "b", "b", "b", notes = "untagged", date = DATE_1)
      insertTransaction("c", "c", "c", "c", notes = "#food 2", date = DATE_2)
      insertTransaction("d", "c", "c", "c", notes = "#food 3", date = DATE_3)
    }
    advanceUntilIdle()

    val source =
      TransactionsPagingSource(
        transactionDao = transactions,
        tagsDao = tags,
        spec = TransactionsSpec(tagSpec = TagSpec.SpecificTag(TagId("food"))),
      )

    // when
    val firstPage =
      source.load(LoadParams.Refresh(key = null, loadSize = 2, placeholdersEnabled = false))
    val secondPage =
      source.load(LoadParams.Append(key = 1, loadSize = 2, placeholdersEnabled = false))

    // then
    assertThat(firstPage)
      .isPage()
      .withData(
        transaction("d", "c", "c", "c", notes = "#food 3", date = DATE_3),
        transaction("c", "c", "c", "c", notes = "#food 2", date = DATE_2),
      )
      .withPrevKey(null)
      .withNextKey(1)
    assertThat(secondPage)
      .isPage()
      .withData(transaction("a", "a", "a", "a", notes = "#food 1", date = DATE_1))
      .withPrevKey(0)
      .withNextKey(null)
  }

  @Test
  fun `View model pages whole transactions`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a", date = DATE_1)
      insertTransaction(id = "d", account = "c", category = "c", payee = "c", date = DATE_2)
    }
    advanceUntilIdle()

    // when
    val snapshot = viewModel.pagingData.asSnapshot()

    // then
    assertThat(snapshot).containsExactly(DATED_D, DATED_A)
  }

  @Test
  fun `Editing an existing transaction refreshes the list`() = runTest {
    // given
    buildViewModel(AllAccounts)
    transactions.insertTransaction(id = "a", account = "a", category = "a", payee = "a")
    advanceUntilIdle()
    assertThat(viewModel.pagingData.asSnapshot()).containsExactly(TRANSACTION_A)

    // when
    val edit = LocalChange("transactions", row = "a", column = "notes", MessageValue.String("New"))
    sync.sendMessages(listOf(edit))
    advanceUntilIdle()

    // then
    assertThat(viewModel.pagingData.asSnapshot()).containsExactly(TRANSACTION_A.copy(notes = "New"))
  }

  @Test
  fun `Density defaults to compact and is remembered`() = runTest {
    // given
    buildViewModel(AllAccounts)

    viewModel.density.test {
      assertThatNextEmissionIsEqualTo(Compact)

      // when
      viewModel.setDensity(Dense)

      // then
      assertThatNextEmissionIsEqualTo(Dense)
    }

    // and a new view model picks it up
    val other = factory.create(TransactionsSpec(AllAccounts))
    other.density.test { assertThatNextEmissionIsEqualTo(Dense) }
    other.viewModelScope.cancel()
  }

  @Test
  fun `Only a linked account can bank sync`() = runTest {
    // given
    buildViewModel(SpecificAccount(LINKED))
    val unlinked = factory.create(TransactionsSpec(SpecificAccount(AccountId("a"))))
    val all = factory.create(TransactionsSpec(AllAccounts))
    advanceUntilIdle()

    // then
    viewModel.canBankSync.test { assertThatNextEmissionIsEqualTo(true) }
    unlinked.canBankSync.test { assertThatNextEmissionIsEqualTo(false) }
    all.canBankSync.test { assertThatNextEmissionIsEqualTo(false) }

    unlinked.viewModelScope.cancel()
    all.viewModelScope.cancel()
  }

  @Test
  fun `Bank sync starts the controller for this account only`() = runTest {
    // given
    buildViewModel(SpecificAccount(LINKED))
    val all = factory.create(TransactionsSpec(AllAccounts))

    // when
    all.bankSync()

    // then
    assertThat(bankSync.started).isEmpty()

    // when
    viewModel.bankSync()

    // then
    assertThat(bankSync.started).containsExactly(setOf(LINKED))

    all.viewModelScope.cancel()
  }

  @Test
  fun `Is syncing while this account is pending`() = runTest {
    // given
    buildViewModel(SpecificAccount(LINKED))

    viewModel.isBankSyncing.test {
      assertThatNextEmissionIsEqualTo(false)

      // when
      bankSync.running(pending = listOf(AccountId("a"), LINKED))

      // then
      assertThatNextEmissionIsEqualTo(true)

      // when
      bankSync.running(pending = listOf(AccountId("a")))

      // then
      assertThatNextEmissionIsEqualTo(false)
    }
  }

  @Test
  fun `A finished sync only announces this account's results`() = runTest {
    // given
    buildViewModel(SpecificAccount(LINKED))
    val other = BankSyncResult.Failed(AccountId("a"), "Amex", BankSyncError.Internal(null))
    val synced =
      BankSyncResult.Synced(
        LINKED,
        "Linked",
        added = listOf(TransactionId("t")),
        updated = emptyList(),
      )

    viewModel.bankSyncFinished.test {
      // when
      bankSync.finish(other)
      bankSync.finish(other, synced)

      // then
      assertThatNextEmissionIsEqualTo(BankSyncSummary.Synced("Linked", added = 1, updated = 0))
    }
  }

  private companion object {
    val LINKED = AccountId("linked")

    val TRANSACTION_A = transaction(id = "a", account = "a", category = "a", payee = "a")
    val TRANSACTION_B = transaction(id = "b", account = "b", category = "b", payee = "b")
    val TRANSACTION_C = transaction(id = "c", account = "c", category = "c", payee = "c")

    val DATED_A = TRANSACTION_A.copy(date = DATE_1)
    val DATED_B = TRANSACTION_B.copy(date = DATE_1)
    val DATED_C = TRANSACTION_C.copy(date = DATE_1)
    val DATED_D = transaction(id = "d", account = "c", category = "c", payee = "c", date = DATE_2)
    val DATED_E = transaction(id = "e", account = "c", category = "c", payee = "c", date = DATE_2)
    val DATED_F = transaction(id = "f", account = "c", category = "c", payee = "c", date = DATE_3)
  }

  @DependencyGraph(AppScope::class)
  internal interface TestAppGraph : AppGraph {
    val runLevelController: RunLevelController
    val runLevelState: RunLevelState
  }
}
