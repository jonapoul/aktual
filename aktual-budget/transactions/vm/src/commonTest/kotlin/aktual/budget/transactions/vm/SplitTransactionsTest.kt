package aktual.budget.transactions.vm

import aktual.budget.model.AccountSpec.AllAccounts
import aktual.budget.model.Amount
import aktual.budget.model.TagId
import aktual.budget.model.TagSpec
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsSpec
import aktual.budget.transactions.vm.test.DATED_D
import aktual.budget.transactions.vm.test.DATE_1
import aktual.budget.transactions.vm.test.DATE_2
import aktual.budget.transactions.vm.test.DATE_3
import aktual.budget.transactions.vm.test.TRANSACTION_A
import aktual.budget.transactions.vm.test.asChild
import aktual.budget.transactions.vm.test.asParent
import aktual.budget.transactions.vm.test.insertTransaction
import aktual.budget.transactions.vm.test.isPage
import aktual.budget.transactions.vm.test.transaction
import aktual.budget.transactions.vm.test.withBalance
import aktual.budget.transactions.vm.test.withData
import aktual.budget.transactions.vm.test.withNextOffset
import aktual.budget.transactions.vm.test.withPrevOffset
import aktual.test.assertThatNextEmissionIsEqualTo
import androidx.paging.PagingSource.LoadParams
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class SplitTransactionsTest : TransactionsViewModelTestBase() {
  @Test
  fun `Split children hang off their parent and aren't counted twice`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("a", "a", "a", "a", date = DATE_1)
      insertTransaction("p", "a", null, "a", date = DATE_2, amount = 100.0, isParent = true)
      insertTransaction("p1", "a", "b", "b", date = DATE_2, amount = 60.0, parent = "p")
      insertTransaction("p2", "a", "c", "b", date = DATE_2, amount = 40.0, parent = "p")
      insertTransaction("d", "c", "c", "c", date = DATE_3)
    }
    advanceUntilIdle()

    // when
    val snapshot = viewModel.pagingData.asSnapshot()

    // then
    assertThat(snapshot)
      .containsExactly(
        DATED_D.copy(date = DATE_3).withBalance(346.90),
        transaction("p", "a", null, "a", date = DATE_2, amount = 100.0, balance = 223.45)
          .asParent(
            payee = "B&Q",
            transaction("p1", "a", "b", "b", date = DATE_2, amount = 60.0),
            transaction("p2", "a", "c", "b", date = DATE_2, amount = 40.0),
          ),
        TRANSACTION_A.withBalance(123.45),
      )
    viewModel.balance
      .dropWhile { it != Amount(346.90) }
      .test { assertThatNextEmissionIsEqualTo(Amount(346.90)) }
  }

  @Test
  fun `A split whose parts don't add up shows what's left to assign`() = runTest {
    // given
    buildViewModel(AllAccounts)
    val error = buildJsonObject {
      put("type", "SplitTransactionError")
      put("version", 1)
      put("difference", 4000)
    }
    with(transactions) {
      insertTransaction("p", "a", null, "a", amount = 100.0, isParent = true, error = error)
      insertTransaction("p1", "a", "b", "b", amount = 60.0, parent = "p")
    }
    advanceUntilIdle()

    // when
    val snapshot = viewModel.pagingData.asSnapshot()

    // then
    assertThat(snapshot)
      .containsExactly(
        transaction("p", "a", null, "a", amount = 100.0, balance = 100.0)
          .asParent(payee = "B&Q", transaction("p1", "a", "b", "b", amount = 60.0))
          .copy(splitRemaining = Amount(40.0)),
      )
  }

  @Test
  fun `A split shows the most common payee of its children`() = runTest {
    // given
    buildViewModel(AllAccounts)
    with(transactions) {
      insertTransaction("common", "a", null, "c", isParent = true)
      insertTransaction("common1", "a", "a", "a", parent = "common")
      insertTransaction("common2", "a", "a", "b", parent = "common")
      insertTransaction("common3", "a", "a", "b", parent = "common")
      insertTransaction("tie", "a", null, "c", isParent = true)
      insertTransaction("tie1", "a", "a", "a", parent = "tie")
      insertTransaction("tie2", "a", "a", "b", parent = "tie")
      insertTransaction("none", "a", null, "c", isParent = true)
      insertTransaction("none1", "a", "a", "missing", parent = "none")
    }
    advanceUntilIdle()

    // when
    val snapshot = viewModel.pagingData.asSnapshot()

    // then
    assertThat(snapshot.associate { it.id.toString() to it.payee })
      .isEqualTo(mapOf("common" to "B&Q", "none" to null, "tie" to "Argos"))
  }

  @Test
  fun `Uncategorised list shows split children as rows`() = runTest {
    // given
    buildViewModel(AllAccounts)
    tags.insert(id = TagId("food"), tag = "food", color = null, description = null)
    with(transactions) {
      insertTransaction("p", "a", null, "a", isParent = true)
      insertTransaction("p1", "a", null, "a", parent = "p")
      insertTransaction("p2", "a", null, "b", notes = "#food", parent = "p")
      insertTransaction("p3", "a", "c", "c", notes = "#food", parent = "p")
    }
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

    // then
    assertThat(uncategorised.load(params))
      .isPage()
      .withData(
        transaction("p1", "a", null, "a").asChild(),
        transaction("p2", "a", null, "b", notes = "#food").asChild(),
      )
    assertThat(uncategorisedWithTag.load(params))
      .isPage()
      .withData(transaction("p2", "a", null, "b", notes = "#food").asChild())
  }

  @Test
  fun `Tag list shows a split as its parent with the matching children`() = runTest {
    // given
    buildViewModel(AllAccounts)
    tags.insert(id = TagId("food"), tag = "food", color = null, description = null)
    with(transactions) {
      // only a child matches
      insertTransaction("p", "a", null, "a", date = DATE_3, isParent = true)
      insertTransaction("p1", "a", "a", "a", notes = "#food", date = DATE_3, parent = "p")
      insertTransaction("p2", "a", "a", "a", date = DATE_3, parent = "p")
      // the parent and two children match
      insertTransaction("q", "a", null, "a", notes = "#food", date = DATE_2, isParent = true)
      insertTransaction("q1", "a", "a", "a", notes = "#food", date = DATE_2, parent = "q")
      insertTransaction("q2", "a", "a", "a", date = DATE_2, parent = "q")
      insertTransaction("q3", "a", "a", "a", notes = "#food", date = DATE_2, parent = "q")
      // only the parent matches
      insertTransaction("r", "a", null, "a", notes = "#food", date = DATE_1, isParent = true)
      insertTransaction("r1", "a", "a", "a", date = DATE_1, parent = "r")
      insertTransaction("r2", "a", "a", "a", date = DATE_1, parent = "r")
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

    // then
    assertThat(result)
      .isPage()
      .withData(
        transaction("p", "a", null, "a", date = DATE_3)
          .asParent(
            payee = "Argos",
            transaction("p1", "a", "a", "a", notes = "#food", date = DATE_3),
            totalChildren = 2,
          ),
        transaction("q", "a", null, "a", notes = "#food", date = DATE_2)
          .asParent(
            payee = "Argos",
            transaction("q1", "a", "a", "a", notes = "#food", date = DATE_2),
            transaction("q3", "a", "a", "a", notes = "#food", date = DATE_2),
            totalChildren = 3,
          ),
        transaction("r", "a", null, "a", notes = "#food", date = DATE_1)
          .asParent(payee = "Argos", totalChildren = 2),
      )
      .withPrevOffset(null)
      .withNextOffset(null)
  }

  @Test
  fun `A split with several matching children takes one slot in a tag list`() = runTest {
    // given
    buildViewModel(AllAccounts)
    tags.insert(id = TagId("food"), tag = "food", color = null, description = null)
    with(transactions) {
      insertTransaction("p", "a", null, "a", date = DATE_3, isParent = true)
      insertTransaction("p1", "a", "a", "a", notes = "#food", date = DATE_3, parent = "p")
      insertTransaction("p2", "a", "a", "a", notes = "#food", date = DATE_3, parent = "p")
      insertTransaction("a", "a", "a", "a", notes = "#food", date = DATE_2)
      insertTransaction("b", "b", "b", "b", notes = "#food", date = DATE_1)
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
        transaction("p", "a", null, "a", date = DATE_3)
          .asParent(
            payee = "Argos",
            transaction("p1", "a", "a", "a", notes = "#food", date = DATE_3),
            transaction("p2", "a", "a", "a", notes = "#food", date = DATE_3),
          ),
        transaction("a", "a", "a", "a", notes = "#food", date = DATE_2),
      )
      .withPrevOffset(null)
      .withNextOffset(2)
    assertThat(secondPage)
      .isPage()
      .withData(transaction("b", "b", "b", "b", notes = "#food", date = DATE_1))
      .withPrevOffset(2)
      .withNextOffset(null)
  }

  @Test
  fun `Splits expand and collapse`() = runTest {
    // given
    buildViewModel(AllAccounts)
    val id = TransactionId("p")

    viewModel.toggledSplits.test {
      assertThatNextEmissionIsEqualTo(persistentSetOf())

      // when
      viewModel.toggleSplit(id)

      // then
      assertThatNextEmissionIsEqualTo(persistentSetOf(id))

      // when
      viewModel.toggleSplit(id)

      // then
      assertThatNextEmissionIsEqualTo(persistentSetOf())
    }
  }

  @Test
  fun `Changing whether splits start expanded clears the toggles`() = runTest {
    // given
    buildViewModel(AllAccounts)
    val id = TransactionId("p")
    advanceUntilIdle()
    viewModel.toggleSplit(id)

    viewModel.toggledSplits.test {
      assertThatNextEmissionIsEqualTo(persistentSetOf(id))

      // when
      viewModel.setExpandSplits(true)

      // then
      assertThatNextEmissionIsEqualTo(persistentSetOf())
    }
    viewModel.expandSplits.test { assertThatNextEmissionIsEqualTo(true) }
  }
}
