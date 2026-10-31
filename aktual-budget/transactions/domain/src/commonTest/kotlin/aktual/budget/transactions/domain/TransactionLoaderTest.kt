package aktual.budget.transactions.domain

import aktual.budget.db.dao.TransactionDetail
import aktual.budget.db.dao.TransactionRow
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
      prop(LoadedTransaction::detail).prop(TransactionDetail::cleared).isFalse()
      prop(LoadedTransaction::detail).prop(TransactionDetail::reconciled).isFalse()
      prop(LoadedTransaction::detail).prop(TransactionDetail::parent).isNull()
      prop(LoadedTransaction::detail).prop(TransactionDetail::row).all {
        prop(TransactionRow::id).isEqualTo(id)
        prop(TransactionRow::accountName).isEqualTo(ACCOUNT.value)
        prop(TransactionRow::payeeName).isEqualTo("Tesco")
        prop(TransactionRow::categoryName).isEqualTo("Groceries")
        prop(TransactionRow::notes).isEqualTo("Weekly shop")
        prop(TransactionRow::amount).isEqualTo(-1234L)
      }
    }
  }

  @Test
  fun `Load a split parent with its parts`() = runWriterTest {
    val (parent, children) = insertSplit()

    val loaded = loader().load(parent)

    assertThat(loaded).isNotNull().all {
      prop(LoadedTransaction::detail)
        .prop(TransactionDetail::row)
        .prop(TransactionRow::id)
        .isEqualTo(parent)
      prop(LoadedTransaction::detail)
        .prop(TransactionDetail::row)
        .prop(TransactionRow::isParent)
        .isTrue()
      prop(LoadedTransaction::children)
        .transform { rows -> rows.map { it.id } }
        .containsExactly(*children.toTypedArray())
      prop(LoadedTransaction::parts)
        .containsExactly(
          SplitPart(children[0], Amount(-1000), category = GROCERIES, sortOrder = 2),
          SplitPart(children[1], Amount(-2000), category = HOUSEHOLD, sortOrder = 1),
        )
    }
  }

  @Test
  fun `Saving a split's changes writes its parts`() = runWriterTest {
    val (parent, children) = insertSplit()
    val saved = checkNotNull(loader().load(parent)).draft
    val draft =
      saved
        .withoutPart(children[1])
        .addPart { TransactionId("added") }
        .fillRemaining(TransactionId("added"))

    writer.write { transactionDiff(parent, saved, draft)?.let { write(it) } }

    assertThat(loader().load(parent)?.parts)
      .isEqualTo(
        listOf(
          SplitPart(children[0], Amount(-1000), category = GROCERIES, sortOrder = 2),
          SplitPart(TransactionId("added"), Amount(-2000), sortOrder = 1),
        ),
      )
  }

  @Test
  fun `A split part loads as its whole split`() = runWriterTest {
    val (parent, children) = insertSplit()

    val loaded = loader().load(children.first())

    assertThat(loaded).isNotNull().all {
      prop(LoadedTransaction::detail)
        .prop(TransactionDetail::row)
        .prop(TransactionRow::id)
        .isEqualTo(parent)
      prop(LoadedTransaction::children)
        .transform { rows -> rows.map { it.id } }
        .containsExactly(*children.toTypedArray())
    }
  }

  @Test
  fun `Balance after counts the transaction and everything older in its account`() = runWriterTest {
    insertAccount(ACCOUNT)
    insertAccount(OTHER)
    val ids = writer.write {
      listOf(
        insert(
          NewTransaction(account = ACCOUNT, date = LocalDate(2026, 9, 14), amount = Amount(100)),
        ),
        insert(NewTransaction(account = ACCOUNT, date = DATE, amount = Amount(20), sortOrder = 1)),
        insert(NewTransaction(account = ACCOUNT, date = DATE, amount = Amount(3), sortOrder = 2)),
        insert(NewTransaction(account = OTHER, date = DATE, amount = Amount(4000))),
      )
    }

    assertThat(ids.map { loader().load(it)?.balanceAfter }).containsExactly(100L, 120L, 123L, 4000L)
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
    val OTHER = AccountId("other")
    val GROCERIES = CategoryId("groceries")
    val HOUSEHOLD = CategoryId("household")
    val DATE = LocalDate(2026, 9, 15)
  }
}
