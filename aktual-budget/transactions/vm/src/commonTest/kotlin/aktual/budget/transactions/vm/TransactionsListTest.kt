package aktual.budget.transactions.vm

import aktual.budget.model.AccountId
import aktual.budget.model.AccountSpec.AllAccounts
import aktual.budget.model.AccountSpec.SpecificAccount
import aktual.budget.model.CategoryId
import aktual.budget.model.CategorySpec
import aktual.budget.model.TagId
import aktual.budget.model.TagSpec
import aktual.budget.model.TransactionsSpec
import aktual.budget.model.localChange
import aktual.budget.tags.domain.TagLabel
import aktual.budget.transactions.vm.test.DATED_A
import aktual.budget.transactions.vm.test.DATED_B
import aktual.budget.transactions.vm.test.DATED_C
import aktual.budget.transactions.vm.test.DATED_D
import aktual.budget.transactions.vm.test.DATED_E
import aktual.budget.transactions.vm.test.DATED_F
import aktual.budget.transactions.vm.test.DATE_1
import aktual.budget.transactions.vm.test.DATE_2
import aktual.budget.transactions.vm.test.DATE_3
import aktual.budget.transactions.vm.test.TRANSACTION_A
import aktual.budget.transactions.vm.test.TRANSACTION_B
import aktual.budget.transactions.vm.test.TRANSACTION_C
import aktual.budget.transactions.vm.test.insertTransaction
import aktual.budget.transactions.vm.test.isPage
import aktual.budget.transactions.vm.test.transaction
import aktual.budget.transactions.vm.test.withBalance
import aktual.budget.transactions.vm.test.withData
import aktual.budget.transactions.vm.test.withNextOffset
import aktual.budget.transactions.vm.test.withPrevOffset
import androidx.paging.PagingSource.LoadParams
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

internal class TransactionsListTest : TransactionsViewModelTestBase() {
  @Test
  fun `Tags follow the database`() = runTest {
    buildViewModel(AllAccounts)

    viewModel.tags.test {
      assertThat(awaitItem()).isEmpty()

      tags.insert(id = TagId("f"), tag = "food", color = "#FF0000", description = null)
      tags.insert(id = TagId("b"), tag = "Bills", color = null, description = null)
      var labels = awaitItem()
      while (labels.size < 2) labels = awaitItem()
      assertThat(labels)
        .containsExactly(TagLabel(TagId("b"), "Bills", null), TagLabel(TagId("f"), "food", Red))
    }
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
    assertThat(result).isPage().withData(emptyList()).withPrevOffset(null).withNextOffset(null)
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
      .withData( // Same-day ties are broken by id, each with the balance after it
        TRANSACTION_A.withBalance(370.35),
        TRANSACTION_B.withBalance(246.90),
        TRANSACTION_C.withBalance(123.45),
      )
      .withPrevOffset(null)
      .withNextOffset(null) // No more pages since we loaded fewer items than page size
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
      .withData(TRANSACTION_A.withBalance(123.45))
      .withPrevOffset(null)
      .withNextOffset(null) // No more pages since we loaded fewer items than page size
  }

  @Test
  fun `Off budget label shows whether or not the transaction is categorised`() = runTest {
    // given
    buildViewModel(AllAccounts)
    accounts.insert(id = AccountId("o"), name = "Offshore", offBudget = true)
    with(transactions) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a")
      insertTransaction(id = "o", account = "o", category = null, payee = "a")
      insertTransaction(id = "p", account = "o", category = "b", payee = "b")
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
    val uncategorised =
      transaction(id = "o", account = "a", category = null, payee = "a")
        .copy(account = "Offshore", needsCategory = false, specialCategory = OffBudget)
    val categorised =
      transaction(id = "p", account = "a", category = "b", payee = "b")
        .copy(account = "Offshore", specialCategory = OffBudget)
    assertThat(result)
      .isPage()
      .withData(
        TRANSACTION_A.withBalance(370.35),
        uncategorised.withBalance(246.90),
        categorised.withBalance(123.45),
      )
      .withPrevOffset(null)
      .withNextOffset(null)
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
        transaction(id = "a", account = "a", category = "a", payee = "a", notes = "lunch #food"),
        transaction(id = "d", account = "c", category = "c", payee = "c", notes = "#FOOD again"),
      )
      .withPrevOffset(expected = null)
      .withNextOffset(expected = null)
  }

  @Test
  fun `A category's transactions in one month`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a")
      insertTransaction(id = "b", account = "b", category = "b", payee = "b")
      insertTransaction(id = "c", account = "c", category = "a", payee = "c", date = JULY)
    }
    advanceUntilIdle()
    val spec =
      TransactionsSpec(
        categorySpec = CategorySpec.SpecificCategory(CategoryId("a"), YearMonth(2025, 6)),
      )
    val source =
      TransactionsPagingSource(transactionDao = transactions, tagsDao = tags, spec = spec)
    val params = LoadParams.Refresh<PageKey>(key = null, loadSize = 50, placeholdersEnabled = false)

    // then - no balances, since the list only holds part of each account
    assertThat(source.load(params))
      .isPage()
      .withData(transaction(id = "a", account = "a", category = "a", payee = "a"))
      .withPrevOffset(expected = null)
      .withNextOffset(expected = null)

    // and the title names the category and month
    val categoryViewModel = factory.create(spec)
    assertThat(categoryViewModel.loadedAccount.first { it != Loading })
      .isEqualTo(LoadedAccount.SpecificCategory("Additional", YearMonth(2025, 6)))
  }

  @Test
  fun `Uncategorised transactions`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction(id = "a", account = "a", category = "a", payee = "a")
      insertTransaction(id = "b", account = "b", category = null, payee = "b")
      insertTransaction(id = "c", account = "c", category = null, payee = "c", notes = "#food")
    }
    tags.insert(id = TagId("food"), tag = "food", color = null, description = null)
    advanceUntilIdle()

    val uncategorised =
      TransactionsPagingSource(
        transactionDao = transactions,
        tagsDao = tags,
        spec = TransactionsSpec(categorySpec = Uncategorised),
      )
    val uncategorisedWithTag =
      TransactionsPagingSource(
        transactionDao = transactions,
        tagsDao = tags,
        spec =
          TransactionsSpec(
            tagSpec = TagSpec.SpecificTag(TagId("food")),
            categorySpec = Uncategorised,
          ),
      )
    val params = LoadParams.Refresh<PageKey>(key = null, loadSize = 50, placeholdersEnabled = false)

    // then - no balances, since the list only holds part of each account
    assertThat(uncategorised.load(params))
      .isPage()
      .withData(
        transaction(id = "b", account = "b", category = null, payee = "b"),
        transaction(id = "c", account = "c", category = null, payee = "c", notes = "#food"),
      )
    assertThat(uncategorisedWithTag.load(params))
      .isPage()
      .withData(transaction(id = "c", account = "c", category = null, payee = "c", notes = "#food"))
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
      .withData(
        DATED_F.withBalance(740.70),
        DATED_D.withBalance(617.25),
        DATED_E.withBalance(493.80),
        DATED_A.withBalance(370.35),
        DATED_B.withBalance(246.90),
        DATED_C.withBalance(123.45),
      )
      .withPrevOffset(null)
      .withNextOffset(null) // No more pages since we loaded fewer items than page size
  }

  @Test
  fun `A transfer shows the other account and its direction`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("out", "a", null, "t", amount = -50.0)
      insertTransaction("in", "a", null, "t", amount = 50.0)
      insertTransaction("split", "a", null, "c", isParent = true)
      insertTransaction("split1", "a", "a", "t", amount = -10.0, parent = "split")
      insertTransaction("other", "a", "a", "a")
    }
    advanceUntilIdle()

    // when
    val snapshot = viewModel.pagingData.asSnapshot()

    // then
    assertThat(snapshot.associate { it.id.toString() to (it.payee to it.transfer) })
      .isEqualTo(
        mapOf(
          "in" to ("Barclays" to From),
          "other" to ("Argos" to null),
          "out" to ("Barclays" to To),
          "split" to ("Barclays" to To),
        ),
      )
  }

  @Test
  fun `Editing an existing transaction refreshes the list`() = runTest {
    // given
    buildViewModel(AllAccounts)
    transactions.insertTransaction(id = "a", account = "a", category = "a", payee = "a")
    advanceUntilIdle()
    assertThat(viewModel.pagingData.asSnapshot()).containsExactly(TRANSACTION_A.withBalance(123.45))

    // when
    val edit = localChange("transactions", row = "a", column = "notes", "New")
    sync.sendMessages(listOf(edit))
    advanceUntilIdle()

    // then
    assertThat(viewModel.pagingData.asSnapshot())
      .containsExactly(TRANSACTION_A.copy(notes = "New").withBalance(123.45))
  }

  private companion object {
    val JULY = LocalDate(2025, 7, 1)
  }
}
