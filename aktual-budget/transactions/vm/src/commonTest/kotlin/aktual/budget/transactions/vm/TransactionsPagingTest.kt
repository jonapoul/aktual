package aktual.budget.transactions.vm

import aktual.budget.model.AccountId
import aktual.budget.model.AccountSpec
import aktual.budget.model.AccountSpec.AllAccounts
import aktual.budget.model.AccountSpec.SpecificAccount
import aktual.budget.model.Amount
import aktual.budget.model.TagId
import aktual.budget.model.TagSpec
import aktual.budget.model.TransactionsSpec
import aktual.budget.model.localChange
import aktual.budget.transactions.vm.test.DATED_A
import aktual.budget.transactions.vm.test.DATED_B
import aktual.budget.transactions.vm.test.DATED_C
import aktual.budget.transactions.vm.test.DATED_D
import aktual.budget.transactions.vm.test.DATED_E
import aktual.budget.transactions.vm.test.DATED_F
import aktual.budget.transactions.vm.test.DATE_1
import aktual.budget.transactions.vm.test.DATE_2
import aktual.budget.transactions.vm.test.DATE_3
import aktual.budget.transactions.vm.test.asPage
import aktual.budget.transactions.vm.test.insertTransaction
import aktual.budget.transactions.vm.test.isPage
import aktual.budget.transactions.vm.test.transaction
import aktual.budget.transactions.vm.test.withBalance
import aktual.budget.transactions.vm.test.withData
import aktual.budget.transactions.vm.test.withNextOffset
import aktual.budget.transactions.vm.test.withPrevOffset
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingState
import androidx.paging.testing.asSnapshot
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

internal class TransactionsPagingTest : TransactionsViewModelTestBase() {
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

    // then - first page contains first 2 items, newest first
    assertThat(firstPage)
      .isPage()
      .withData(DATED_F.withBalance(740.70), DATED_D.withBalance(617.25))
      .withPrevOffset(null)
      .withNextOffset(2)

    // when - load second page
    val secondPage =
      source.load(LoadParams.Append(key = PageKey(2), loadSize = 2, placeholdersEnabled = false))

    // then - second page contains next 2 items, its balances carrying on from the first page
    assertThat(secondPage)
      .isPage()
      .withData(DATED_E.withBalance(493.80), DATED_A.withBalance(370.35))
      .withPrevOffset(2)
      .withNextOffset(4)

    // when - load third page
    val thirdPage =
      source.load(LoadParams.Append(key = PageKey(4), loadSize = 2, placeholdersEnabled = false))

    // then - third page contains remaining items
    assertThat(thirdPage)
      .isPage()
      .withData(DATED_B.withBalance(246.90), DATED_C.withBalance(123.45))
      .withPrevOffset(4)
      .withNextOffset(6) // More pages possible since we loaded exactly the page size
  }

  @Test
  fun `Pages don't overlap when the first load is bigger than the rest`() = runTest {
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

    // when - the first load is twice the page size, as Paging's initial load is
    val firstPage =
      source.load(LoadParams.Refresh(key = null, loadSize = 4, placeholdersEnabled = false))
    val secondPage =
      source.load(LoadParams.Append(key = PageKey(4), loadSize = 2, placeholdersEnabled = false))

    // then
    assertThat(firstPage)
      .isPage()
      .withData(
        DATED_F.withBalance(740.70),
        DATED_D.withBalance(617.25),
        DATED_E.withBalance(493.80),
        DATED_A.withBalance(370.35),
      )
      .withPrevOffset(null)
      .withNextOffset(4)
    assertThat(secondPage)
      .isPage()
      .withData(DATED_B.withBalance(246.90), DATED_C.withBalance(123.45))
      .withPrevOffset(4)
      .withNextOffset(6)
  }

  @Test
  fun `Prepending stops at the start of the list`() = runTest {
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

    // when - only one row sits before the key, fewer than the page size
    val page =
      source.load(LoadParams.Prepend(key = PageKey(1), loadSize = 2, placeholdersEnabled = false))

    // then
    assertThat(page)
      .isPage()
      .withData(DATED_F.withBalance(740.70))
      .withPrevOffset(null)
      .withNextOffset(1)
  }

  @Test
  fun `Balances carry across pages loaded down and back up`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("t1", "a", "a", "a", date = DATE_1, amount = 10.0)
      insertTransaction("t2", "c", "c", "c", date = DATE_1, amount = 20.0)
      insertTransaction("t3", "c", "c", "c", date = DATE_2, amount = -5.0)
      insertTransaction("t4", "a", "a", "a", date = DATE_2, amount = 7.0)
      insertTransaction("p", "c", null, "c", date = DATE_3, amount = 100.0, isParent = true)
      insertTransaction("p1", "c", "a", "c", date = DATE_3, amount = 60.0, parent = "p")
      insertTransaction("p2", "c", "b", "c", date = DATE_3, amount = 40.0, parent = "p")
      insertTransaction("t5", "c", "c", "c", date = DATE_3, amount = 3.0)
      insertTransaction("t6", "b", "b", "b", date = DATE_3, amount = 1.0)
    }
    sync.sendMessages(listOf(localChange("transactions", row = "t3", "reconciled", true)))
    advanceUntilIdle()
    val sources =
      mapOf(
        "all" to pagingSource(AllAccounts),
        "account" to pagingSource(SpecificAccount(AccountId("c"))),
        "group" to pagingSource(AccountSpec.Group(OnBudget)),
        "hidden" to pagingSource(SpecificAccount(AccountId("c")), hideReconciled = true),
      )

    sources.forEach { (name, source) ->
      // when
      val whole = source.load(LoadParams.Refresh(null, 50, false)).asPage().data
      val (down, up) = source.pagedDownAndUp(pageSize = 2)

      // then
      assertThat(down, name).isEqualTo(whole)
      assertThat(up, name).isEqualTo(whole)
    }
    val all = sources.getValue("all").load(LoadParams.Refresh(null, 50, false)).asPage().data
    assertThat(all.first().balance).isEqualTo(Amount(136.0))
  }

  @Test
  fun `A refresh after a change works out the balance again`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("t1", "a", "a", "a", date = DATE_1, amount = 10.0)
      insertTransaction("t2", "b", "b", "b", date = DATE_1, amount = 20.0)
      insertTransaction("t3", "c", "c", "c", date = DATE_2, amount = 30.0)
      insertTransaction("t4", "a", "a", "a", date = DATE_3, amount = 40.0)
      insertTransaction("t5", "b", "b", "b", date = DATE_3, amount = 50.0)
    }
    advanceUntilIdle()
    val old = pagingSource(AllAccounts)
    val first = old.load(LoadParams.Refresh(null, 2, false)).asPage()
    val second = old.load(LoadParams.Append(requireNotNull(first.nextKey), 2, false)).asPage()

    // when an older transaction moves every balance above it
    transactions.insertTransaction("old", "a", "a", "a", date = LocalDate(2025, 5, 1), amount = 5.0)
    advanceUntilIdle()
    val state = PagingState(listOf(first, second), 2, PagingConfig(pageSize = 2), 0)
    val fresh = pagingSource(AllAccounts)
    val refreshed = fresh.load(LoadParams.Refresh(old.getRefreshKey(state), 2, false)).asPage()

    // then
    val whole = fresh.load(LoadParams.Refresh(null, 50, false)).asPage().data
    assertThat(refreshed.data).isEqualTo(whole.subList(2, 4))
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
      source.load(LoadParams.Append(key = PageKey(2), loadSize = 2, placeholdersEnabled = false))

    // then
    assertThat(firstPage)
      .isPage()
      .withData(
        transaction("d", "c", "c", "c", notes = "#food 3", date = DATE_3),
        transaction("c", "c", "c", "c", notes = "#food 2", date = DATE_2),
      )
      .withPrevOffset(null)
      .withNextOffset(2)
    assertThat(secondPage)
      .isPage()
      .withData(transaction("a", "a", "a", "a", notes = "#food 1", date = DATE_1))
      .withPrevOffset(2)
      .withNextOffset(null)
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
    assertThat(snapshot).containsExactly(DATED_D.withBalance(246.90), DATED_A.withBalance(123.45))
  }

  private fun pagingSource(spec: AccountSpec, hideReconciled: Boolean = false) =
    TransactionsPagingSource(
      transactionDao = transactions,
      tagsDao = tags,
      spec = TransactionsSpec(spec),
      hideReconciled = hideReconciled,
    )

  // The whole list, paged down from the top and then back up from the bottom
  private suspend fun <K : Any> PagingSource<K, Transaction>.pagedDownAndUp(
    pageSize: Int,
  ): Pair<List<Transaction>, List<Transaction>> {
    val down = mutableListOf(load(LoadParams.Refresh(null, pageSize, false)).asPage())
    while (true) {
      val key = down.last().nextKey ?: break
      down += load(LoadParams.Append(key, pageSize, false)).asPage()
    }
    val up = mutableListOf(down.last())
    while (true) {
      val key = up.last().prevKey ?: break
      up += load(LoadParams.Prepend(key, pageSize, false)).asPage()
    }
    return down.flatMap { it.data } to up.asReversed().flatMap { it.data }
  }
}
