package aktual.budget.transactions.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class TransactionDiffTest {
  @Test
  fun `Nothing to write when nothing changed`() {
    assertThat(transactionDiff(ID, SAVED, SAVED)).isNull()
  }

  @Test
  fun `Blank notes are the same as none`() {
    val saved = SAVED.copy(notes = null)
    assertThat(transactionDiff(ID, saved, saved.copy(notes = ""))).isNull()
  }

  @Test
  fun `Only changed fields are written`() {
    val draft = SAVED.copy(amount = Amount(-2500L), date = LocalDate(2026, 10, 4), cleared = false)

    assertThat(transactionDiff(ID, SAVED, draft))
      .isEqualTo(
        TransactionUpdate(
          id = ID,
          date = LocalDate(2026, 10, 4),
          amount = Amount(-2500L),
          cleared = false,
        ),
      )
  }

  @Test
  fun `Changed entities are patched`() {
    val draft =
      SAVED.copy(account = AccountId("b"), payee = PayeeId("b"), category = CategoryId("b"))

    assertThat(transactionDiff(ID, SAVED, draft))
      .isEqualTo(
        TransactionUpdate(
          id = ID,
          account = AccountId("b"),
          payee = Patch.To(PayeeId("b")),
          category = Patch.To(CategoryId("b")),
        ),
      )
  }

  @Test
  fun `Cleared fields are written as null`() {
    val draft = SAVED.copy(payee = null, category = null, notes = "")

    assertThat(transactionDiff(ID, SAVED, draft))
      .isEqualTo(
        TransactionUpdate(
          id = ID,
          payee = Patch.To(null),
          category = Patch.To(null),
          notes = Patch.To(null),
        ),
      )
  }

  @Test
  fun `Unlocking only changes reconciled`() {
    val saved = SAVED.copy(reconciled = true)

    assertThat(transactionDiff(ID, saved, saved.copy(reconciled = false)))
      .isEqualTo(TransactionUpdate(id = ID, reconciled = false))
  }

  private companion object {
    val ID = TransactionId("t")

    val SAVED =
      TransactionFields(
        account = AccountId("a"),
        date = LocalDate(2026, 10, 3),
        amount = Amount(-4210L),
        payee = PayeeId("a"),
        category = CategoryId("a"),
        notes = "Weekly shop",
        cleared = true,
        reconciled = false,
      )
  }
}
