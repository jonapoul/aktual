package aktual.budget.transactions.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.TransactionId
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.datetime.LocalDate

internal class TransactionLoaderTest {
  @Test
  fun `Load a transaction with its names and cleared state`() = runWriterTest {
    insertAccount(ACCOUNT)
    insertCategory(GROCERIES, name = "Groceries")
    val id = writer.write {
      val payee = insertPayee("Tesco")
      insert(
        NewTransaction(
          account = ACCOUNT,
          date = DATE,
          amount = Amount(-1234),
          payee = payee,
          category = GROCERIES,
          notes = "Weekly shop",
          cleared = false,
        ),
      )
    }

    val loaded = loader().load(id)

    assertThat(loaded).isNotNull().all {
      prop(LoadedTransaction::children).isEmpty()
      prop("cleared") { it.detail.cleared }.isFalse()
      prop("reconciled") { it.detail.reconciled }.isFalse()
      prop("parent") { it.detail.parent }.isNull()
      prop("row") { it.detail.row }
        .all {
          prop("id") { it.id }.isEqualTo(id)
          prop("accountName") { it.accountName }.isEqualTo(ACCOUNT.value)
          prop("payeeName") { it.payeeName }.isEqualTo("Tesco")
          prop("categoryName") { it.categoryName }.isEqualTo("Groceries")
          prop("notes") { it.notes }.isEqualTo("Weekly shop")
          prop("amount") { it.amount }.isEqualTo(-1234L)
        }
    }
  }

  @Test
  fun `Load a split parent with its parts`() = runWriterTest {
    val (parent, children) = insertSplit()

    val loaded = loader().load(parent)

    assertThat(loaded).isNotNull().all {
      prop("id") { it.detail.row.id }.isEqualTo(parent)
      prop("isParent") { it.detail.row.isParent }.isTrue()
      prop("children") { it.children.map { c -> c.id } }.containsExactly(*children.toTypedArray())
    }
  }

  @Test
  fun `A split part loads as its whole split`() = runWriterTest {
    val (parent, children) = insertSplit()

    val loaded = loader().load(children.first())

    assertThat(loaded).isNotNull().all {
      prop("id") { it.detail.row.id }.isEqualTo(parent)
      prop("children") { it.children.map { c -> c.id } }.containsExactly(*children.toTypedArray())
    }
  }

  @Test
  fun `Missing and deleted transactions load as null`() = runWriterTest {
    insertAccount(ACCOUNT)
    val id = writer.write { insert(NewTransaction(account = ACCOUNT, date = DATE)) }
    writer.write { delete(id) }

    assertThat(loader().load(id)).isNull()
    assertThat(loader().load(TransactionId("missing"))).isNull()
  }

  @Test
  fun `Observing reloads on changes`() = runWriterTest {
    insertAccount(ACCOUNT)
    val id = writer.write { insert(NewTransaction(account = ACCOUNT, date = DATE, notes = "a")) }

    loader().observe(id).test {
      assertThat(awaitItem()?.detail?.row?.notes).isEqualTo("a")

      writer.write { update(TransactionUpdate(id, notes = Patch.To("b"))) }
      assertThat(awaitItem()?.detail?.row?.notes).isEqualTo("b")

      writer.write { delete(id) }
      assertThat(awaitItem()).isNull()
    }
  }

  private fun WriterTestScope.loader() = TransactionLoader(transactionDao)

  // A parent and its two parts, newest part first as childrenOf() orders them
  private suspend fun WriterTestScope.insertSplit(): Pair<TransactionId, List<TransactionId>> {
    insertAccount(ACCOUNT)
    insertCategory(GROCERIES, name = "Groceries")
    insertCategory(HOUSEHOLD, name = "Household")
    return writer.write {
      val parent =
        insert(
          NewTransaction(account = ACCOUNT, date = DATE, amount = Amount(-3000), isParent = true),
        )
      val child1 =
        insert(
          NewTransaction(
            account = ACCOUNT,
            date = DATE,
            amount = Amount(-1000),
            category = GROCERIES,
            parentId = parent,
            sortOrder = 2,
          ),
        )
      val child2 =
        insert(
          NewTransaction(
            account = ACCOUNT,
            date = DATE,
            amount = Amount(-2000),
            category = HOUSEHOLD,
            parentId = parent,
            sortOrder = 1,
          ),
        )
      parent to listOf(child1, child2)
    }
  }

  private companion object {
    val ACCOUNT = AccountId("account")
    val GROCERIES = CategoryId("groceries")
    val HOUSEHOLD = CategoryId("household")
    val DATE = LocalDate(2026, 9, 15)
  }
}
