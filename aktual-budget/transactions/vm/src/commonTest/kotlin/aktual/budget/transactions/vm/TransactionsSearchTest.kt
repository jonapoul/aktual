package aktual.budget.transactions.vm

import aktual.budget.model.AccountId
import aktual.budget.model.AccountSpec
import aktual.budget.model.AccountSpec.AllAccounts
import aktual.budget.model.AccountSpec.SpecificAccount
import aktual.budget.model.Condition
import aktual.budget.model.ConditionSet
import aktual.budget.model.TagId
import aktual.budget.model.TagSpec
import aktual.budget.model.TransactionSearch
import aktual.budget.model.TransactionsSpec
import aktual.budget.model.localChange
import aktual.budget.transactions.vm.test.DATE_1
import aktual.budget.transactions.vm.test.DATE_2
import aktual.budget.transactions.vm.test.DATE_3
import aktual.budget.transactions.vm.test.TRANSACTION_A
import aktual.budget.transactions.vm.test.asPage
import aktual.budget.transactions.vm.test.asParent
import aktual.budget.transactions.vm.test.insertTransaction
import aktual.budget.transactions.vm.test.isPage
import aktual.budget.transactions.vm.test.transaction
import aktual.budget.transactions.vm.test.withBalance
import aktual.budget.transactions.vm.test.withData
import aktual.budget.transactions.vm.test.withNextOffset
import aktual.budget.transactions.vm.test.withPrevOffset
import aktual.test.assertThatNextEmissionIsEqualTo
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlin.test.Test
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive

internal class TransactionsSearchTest : TransactionsViewModelTestBase() {
  @Test
  fun `Search matches names across every account`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("a", "a", "a", "a", date = DATE_3)
      insertTransaction("b", "b", "b", "b", date = DATE_2)
      insertTransaction("c", "c", "c", "c", notes = "argos refund", date = DATE_1)
    }
    advanceUntilIdle()

    // when
    val ids = searchedIds(AllAccounts, TransactionSearch(text = "ARGOS"))

    // then
    assertThat(ids).containsExactly("a", "c")
  }

  @Test
  fun `Search stays within the account`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("a", "a", "a", "a", date = DATE_2)
      insertTransaction("b", "b", "a", "a", date = DATE_1)
    }
    advanceUntilIdle()

    // when
    val ids = searchedIds(SpecificAccount(AccountId("b")), TransactionSearch(text = "argos"))

    // then
    assertThat(ids).containsExactly("b")
  }

  @Test
  fun `Search matches a transfer by its other account`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("out", "a", null, "t", amount = -50.0, date = DATE_2)
      insertTransaction("other", "a", "a", "a", date = DATE_1)
    }
    advanceUntilIdle()

    // when
    val ids = searchedIds(AllAccounts, TransactionSearch(text = "barclays"))

    // then
    assertThat(ids).containsExactly("out")
  }

  @Test
  fun `Search matches amounts and their whole part`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("a", "a", "a", "a", amount = -12.34, date = DATE_3)
      insertTransaction("b", "a", "a", "a", amount = 12.99, date = DATE_2)
      insertTransaction("c", "a", "a", "a", amount = 13.0, date = DATE_1)
    }
    advanceUntilIdle()

    // then
    assertThat(searchedIds(AllAccounts, TransactionSearch(text = "12.34"))).containsExactly("a")
    assertThat(searchedIds(AllAccounts, TransactionSearch(text = "12"))).containsExactly("a", "b")
  }

  @Test
  fun `Search combines text and conditions`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("a", "a", "a", "a", date = DATE_3)
      insertTransaction("b", "a", "b", "a", date = DATE_2)
      insertTransaction("c", "a", "a", "b", date = DATE_1)
    }
    advanceUntilIdle()
    val building = Condition(Category, Is, JsonPrimitive("b"))

    // when
    val search = TransactionSearch(text = "argos", conditions = ConditionSet(listOf(building)))

    // then
    assertThat(searchedIds(AllAccounts, search)).containsExactly("b")
  }

  @Test
  fun `Search shows a split as its parent with the matching children`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("p", "a", null, "a", date = DATE_2, isParent = true)
      insertTransaction("p1", "a", "a", "a", notes = "lunch", date = DATE_2, parent = "p")
      insertTransaction("p2", "a", "b", "a", date = DATE_2, parent = "p")
      insertTransaction("p3", "a", "c", "a", notes = "lunch", date = DATE_2, parent = "p")
      insertTransaction("q", "a", "a", "a", notes = "lunch", date = DATE_1)
    }
    advanceUntilIdle()

    // when
    val result = searchSource(AllAccounts, TransactionSearch(text = "lunch")).refresh()

    // then
    assertThat(result)
      .isPage()
      .withData(
        transaction("p", "a", null, "a", date = DATE_2)
          .asParent(
            payee = "Argos",
            transaction("p1", "a", "a", "a", notes = "lunch", date = DATE_2),
            transaction("p3", "a", "c", "a", notes = "lunch", date = DATE_2),
            totalChildren = 3,
          ),
        transaction("q", "a", "a", "a", notes = "lunch", date = DATE_1),
      )
      .withPrevOffset(null)
      .withNextOffset(null)
  }

  @Test
  fun `Search matches a split parent's own notes`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("p", "a", null, "a", notes = "holiday", date = DATE_2, isParent = true)
      insertTransaction("p1", "a", "a", "a", date = DATE_2, parent = "p")
      insertTransaction("p2", "a", "b", "a", date = DATE_2, parent = "p")
      insertTransaction("q", "a", "a", "a", date = DATE_1)
    }
    advanceUntilIdle()

    // when
    val result = searchSource(AllAccounts, TransactionSearch(text = "holiday")).refresh()

    // then
    assertThat(result)
      .isPage()
      .withData(
        transaction("p", "a", null, "a", notes = "holiday", date = DATE_2)
          .asParent(payee = "Argos", totalChildren = 2),
      )
  }

  @Test
  fun `Search skips deleted categories and payees`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("a", "a", "b", "a", date = DATE_2)
      insertTransaction("b", "a", "a", "b", date = DATE_1)
    }
    sync.sendMessages(
      listOf(
        localChange("categories", row = "b", "tombstone", true),
        localChange("payees", row = "b", "tombstone", true),
      ),
    )
    advanceUntilIdle()

    // then
    assertThat(searchedIds(AllAccounts, TransactionSearch(text = "building"))).isEmpty()
    assertThat(searchedIds(AllAccounts, TransactionSearch(text = "b&q"))).isEmpty()
  }

  @Test
  fun `Search conditions match a split's parent`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("p", "a", null, "a", amount = 100.0, date = DATE_2, isParent = true)
      insertTransaction("p1", "a", "a", "a", amount = 60.0, date = DATE_2, parent = "p")
      insertTransaction("p2", "a", "b", "a", amount = 40.0, date = DATE_2, parent = "p")
      insertTransaction("q", "a", "a", "a", amount = 100.0, date = DATE_1)
    }
    advanceUntilIdle()
    val total = Condition(Amount, Is, JsonPrimitive(10_000))
    val parent = Condition(Parent, Is, JsonPrimitive(true))

    // then
    val byTotal = TransactionSearch(conditions = ConditionSet(listOf(total)))
    assertThat(searchedIds(AllAccounts, byTotal)).containsExactly("p", "q")
    val byParent = TransactionSearch(conditions = ConditionSet(listOf(parent)))
    assertThat(searchedIds(AllAccounts, byParent)).containsExactly("p")
  }

  @Test
  fun `Search keeps a split where its parent sits in the list`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("z", "a", null, "a", date = DATE_1, isParent = true)
      insertTransaction("a", "a", "a", "a", notes = "lunch", date = DATE_1, parent = "z")
      insertTransaction("q", "a", "a", "a", notes = "lunch", date = DATE_1)
    }
    advanceUntilIdle()

    // when
    val ids = searchedIds(AllAccounts, TransactionSearch(text = "lunch"))

    // then
    assertThat(ids).containsExactly("q", "z")
  }

  @Test
  fun `Search skips a split whose parent isn't in the list`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("p", "a", null, "a", date = DATE_2, isParent = true)
      insertTransaction("p1", "b", "a", "a", notes = "lunch", date = DATE_2, parent = "p")
      insertTransaction("q", "b", "a", "a", notes = "lunch", date = DATE_1)
    }
    advanceUntilIdle()

    // when
    val ids = searchedIds(SpecificAccount(AccountId("b")), TransactionSearch(text = "lunch"))

    // then
    assertThat(ids).containsExactly("q")
  }

  @Test
  fun `Search reads LIKE wildcards literally`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("a", "a", "a", "a", notes = "50% off", date = DATE_3)
      insertTransaction("b", "a", "a", "a", notes = "500 off", date = DATE_2)
      insertTransaction("c", "a", "a", "a", notes = "a_b", date = DATE_1)
    }
    advanceUntilIdle()

    // then
    assertThat(searchedIds(AllAccounts, TransactionSearch(text = "50%"))).containsExactly("a")
    assertThat(searchedIds(AllAccounts, TransactionSearch(text = "_"))).containsExactly("c")
  }

  @Test
  fun `Search ignores the case of non-ASCII text`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("a", "a", "a", "a", notes = "Café", date = DATE_2)
      insertTransaction("b", "a", "a", "a", notes = "Cafe", date = DATE_1)
    }
    advanceUntilIdle()

    // when
    val ids = searchedIds(AllAccounts, TransactionSearch(text = "CAFÉ"))

    // then
    assertThat(ids).containsExactly("a")
  }

  @Test
  fun `An empty search lists the account as usual`() = runTest {
    // given
    buildViewModel(AllAccounts)
    transactions.insertTransaction("a", "a", "a", "a")
    advanceUntilIdle()

    // when
    val result = searchSource(AllAccounts, TransactionSearch(text = " ")).refresh()

    // then
    assertThat(result).isPage().withData(TRANSACTION_A.withBalance(123.45))
  }

  @Test
  fun `Searching narrows the list and hides its balance`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("a", "a", "a", "a", date = DATE_2)
      insertTransaction("b", "a", "a", "b", date = DATE_1)
    }
    advanceUntilIdle()

    viewModel.showBalance.test {
      assertThatNextEmissionIsEqualTo(true)

      // when
      viewModel.setSearch(TransactionSearch(text = "argos"))
      advanceUntilIdle()

      // then
      assertThatNextEmissionIsEqualTo(false)
    }
    assertThat(viewModel.pagingData.asSnapshot().map { it.id.toString() to it.balance })
      .containsExactly("a" to null)

    // when
    viewModel.setSearch(TransactionSearch())
    advanceUntilIdle()

    // then
    viewModel.showBalance.test { assertThatNextEmissionIsEqualTo(true) }
    assertThat(viewModel.pagingData.asSnapshot().map { it.id.toString() }).containsExactly("a", "b")
  }

  @Test
  fun `Tag lists ignore a search`() = runTest {
    // given
    buildViewModel(AllAccounts)
    advanceUntilIdle()
    val tagged = factory.create(TransactionsSpec(tagSpec = TagSpec.SpecificTag(TagId("food"))))

    // when
    tagged.setSearch(TransactionSearch(text = "argos"))

    // then
    tagged.showBalance.test { assertThatNextEmissionIsEqualTo(false) }
    tagged.viewModelScope.cancel()
  }

  private fun searchSource(spec: AccountSpec, search: TransactionSearch): TransactionsPagingSource {
    val listSpec = TransactionsSpec(accountSpec = spec)
    return TransactionsPagingSource(
      transactionDao = transactions,
      tagsDao = tags,
      spec = listSpec,
      search = listSpec.activeSearch(search),
    )
  }

  private suspend fun <K : Any> PagingSource<K, Transaction>.refresh() =
    load(LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false))

  private suspend fun searchedIds(spec: AccountSpec, search: TransactionSearch): List<String> =
    searchSource(spec, search).refresh().asPage().data.map { it.id.toString() }
}
