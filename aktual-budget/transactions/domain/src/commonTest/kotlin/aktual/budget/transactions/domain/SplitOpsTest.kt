package aktual.budget.transactions.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isSameInstanceAs
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.datetime.LocalDate

// Ported from packages/loot-core/src/shared/transactions.test.ts
class SplitOpsTest {
  private var nextId = 1
  private val newId = { TransactionId("new-${nextId++}") }

  @Test
  fun `Splitting a transaction works`() {
    val split = draft(amount = 5000, payee = PAYEE, category = CATEGORY).split(newId)

    assertThat(split).all {
      prop("payee") { it.fields.payee }.isNull()
      prop("amount") { it.fields.amount }.isEqualTo(Amount(5000))
      prop(TransactionDraft::splitDifference).isEqualTo(Amount(5000))
      prop(TransactionDraft::parts)
        .containsExactly(
          SplitPart(TransactionId("new-1"), payee = PAYEE, category = CATEGORY, sortOrder = -1),
        )
    }
  }

  @Test
  fun `Splitting a transaction with no amount leaves nothing to assign`() {
    assertThat(draft(amount = 0).split(newId).splitDifference).isNull()
  }

  @Test
  fun `Splitting into empty parts assigns distinct descending sort orders`() {
    val split = draft(amount = 5000).split(newId, count = 2)

    assertThat(split.parts.map { it.sortOrder }).containsExactly(-1L, -2L)
  }

  @Test
  fun `Splitting a split does nothing`() {
    val split = split(2500, part("t2", 2000), part("t3", 500))

    assertThat(split.split(newId)).isSameInstanceAs(split)
  }

  @Test
  fun `Adding a split transaction works`() {
    val split = split(2500, part("t2", 2000, sortOrder = -1), part("t3", 500, sortOrder = -2))

    val added = split.addPart(newId)

    assertThat(added.parts)
      .containsExactly(
        part("t2", 2000, sortOrder = -1),
        part("t3", 500, sortOrder = -2),
        SplitPart(TransactionId("new-1"), amount = Amount(0), sortOrder = -3),
      )
  }

  @Test
  fun `Adding a split transaction reuses the previous child payee`() {
    val split = split(2500, part("t2", 2000, payee = PAYEE))

    assertThat(split.addPart(newId).parts.last().payee).isEqualTo(PAYEE)
  }

  @Test
  fun `Adding to a transaction that isn't split does nothing`() {
    val draft = draft(amount = 2500)

    assertThat(draft.addPart(newId)).isSameInstanceAs(draft)
  }

  @Test
  fun `Updating a split transaction works`() {
    val split = split(2500, part("t2", 2000), part("t3", 500))

    val updated = split.withPart(part("t2", 2200))

    assertThat(updated).all {
      prop(TransactionDraft::splitDifference).isEqualTo(Amount(-200))
      prop(TransactionDraft::parts).containsExactly(part("t2", 2200), part("t3", 500))
    }
  }

  @Test
  fun `Partially updating a split parent preserves amount and does not set error`() {
    val split = split(2500, part("t2", 2000), part("t3", 500))

    val updated = split.withFields(split.fields.copy(notes = "updated note"))

    assertThat(updated).all {
      prop("notes") { it.fields.notes }.isEqualTo("updated note")
      prop("amount") { it.fields.amount }.isEqualTo(Amount(2500))
      prop(TransactionDraft::splitDifference).isNull()
      prop(TransactionDraft::parts).isEqualTo(split.parts)
    }
  }

  @Test
  fun `A new parent payee reaches the parts that had the old one or none`() {
    val other = PayeeId("other")
    val split =
      split(
        2500,
        part("t2", 1000, payee = PAYEE),
        part("t3", 1000, payee = other),
        part("t4", 500),
        payee = PAYEE,
      )

    val updated = split.withFields(split.fields.copy(payee = PayeeId("new")))

    assertThat(updated.parts.map { it.payee })
      .containsExactly(PayeeId("new"), other, PayeeId("new"))
  }

  @Test
  fun `Deleting a split transaction works`() {
    val split = split(2500, part("t2", 2000), part("t3", 500))

    val deleted = split.withoutPart(TransactionId("t2"))

    assertThat(deleted).all {
      prop(TransactionDraft::splitDifference).isEqualTo(Amount(2000))
      prop(TransactionDraft::parts).containsExactly(part("t3", 500))
    }
  }

  @Test
  fun `Deleting all child split transactions works`() {
    val split = split(2500, part("t2", 2000))

    val deleted = split.withoutPart(TransactionId("t2"))

    assertThat(deleted).all {
      prop(TransactionDraft::isSplit).isFalse()
      prop(TransactionDraft::parts).isEmpty()
      // Must delete error if no children
      prop(TransactionDraft::splitDifference).isNull()
      prop("amount") { it.fields.amount }.isEqualTo(Amount(2500))
    }
  }

  @Test
  fun `Deleting an unknown part does nothing`() {
    val split = split(2500, part("t2", 2000))

    assertThat(split.withoutPart(TransactionId("missing"))).isSameInstanceAs(split)
  }

  @Test
  fun `Filling the remainder makes the parts add up`() {
    val split = split(2500, part("t2", 2000), part("t3", 100))

    val filled = split.fillRemaining(TransactionId("t3"))

    assertThat(filled).all {
      prop(TransactionDraft::splitDifference).isNull()
      prop(TransactionDraft::parts).containsExactly(part("t2", 2000), part("t3", 500))
    }
  }

  private fun split(amount: Long, vararg parts: SplitPart, payee: PayeeId? = null) =
    draft(amount, payee).copy(parts = parts.toList())

  private fun draft(amount: Long, payee: PayeeId? = null, category: CategoryId? = null) =
    TransactionDraft(
      TransactionFields(
        account = AccountId("acc-id-1"),
        date = LocalDate(2020, 1, 5),
        amount = Amount(amount),
        payee = payee,
        category = category,
        notes = null,
        cleared = true,
        reconciled = false,
      ),
    )

  private fun part(id: String, amount: Long, payee: PayeeId? = null, sortOrder: Long? = null) =
    SplitPart(TransactionId(id), Amount(amount), payee = payee, sortOrder = sortOrder)

  private companion object {
    val PAYEE = PayeeId("payee-id")
    val CATEGORY = CategoryId("category-id")
  }
}
