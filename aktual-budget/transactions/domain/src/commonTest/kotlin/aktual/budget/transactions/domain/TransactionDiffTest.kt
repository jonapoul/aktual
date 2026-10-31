package aktual.budget.transactions.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.ScheduleId
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
  fun `Linking a schedule patches it`() {
    assertThat(transactionDiff(ID, SAVED, SAVED.copy(schedule = ScheduleId("s"))))
      .isEqualTo(TransactionUpdate(id = ID, schedule = Patch.To(ScheduleId("s"))))
  }

  @Test
  fun `Unlinking a schedule writes it as null`() {
    val saved = SAVED.copy(schedule = ScheduleId("s"))

    assertThat(transactionDiff(ID, saved, saved.copy(schedule = null)))
      .isEqualTo(TransactionUpdate(id = ID, schedule = Patch.To(null)))
  }

  @Test
  fun `Unlocking only changes reconciled`() {
    val saved = SAVED.copy(reconciled = true)

    assertThat(transactionDiff(ID, saved, saved.copy(reconciled = false)))
      .isEqualTo(TransactionUpdate(id = ID, reconciled = false))
  }

  @Test
  fun `Nothing to write when a split didn't change`() {
    assertThat(transactionDiff(ID, SPLIT, SPLIT)).isNull()
  }

  @Test
  fun `Splitting makes the transaction a parent and adds its parts`() {
    val part = SplitPart(TransactionId("p1"), payee = PayeeId("a"), sortOrder = -1)
    val draft = TransactionDraft(SAVED.copy(payee = null), listOf(part))

    assertThat(transactionDiff(ID, TransactionDraft(SAVED), draft))
      .isEqualTo(
        TransactionChanges(
          added =
            listOf(
              NewTransaction(
                account = AccountId("a"),
                date = LocalDate(2026, 10, 3),
                amount = Amount(0),
                payee = PayeeId("a"),
                cleared = true,
                parentId = ID,
                sortOrder = -1,
                id = TransactionId("p1"),
              ),
            ),
          updated =
            listOf(
              TransactionUpdate(
                id = ID,
                isParent = true,
                payee = Patch.To(null),
                error = Patch.To(splitError(Amount(-4210L))),
              ),
            ),
        ),
      )
  }

  @Test
  fun `A changed part is updated along with the split error`() {
    val draft = SPLIT.copy(parts = listOf(PART_1.copy(amount = Amount(-3000L)), PART_2))

    assertThat(transactionDiff(ID, SPLIT, draft))
      .isEqualTo(
        TransactionChanges(
          updated =
            listOf(
              TransactionUpdate(id = ID, error = Patch.To(splitError(Amount(-210L)))),
              TransactionUpdate(id = PART_1.id, amount = Amount(-3000L)),
            ),
        ),
      )
  }

  @Test
  fun `Parts that add up again clear the split error`() {
    val saved = SPLIT.copy(parts = listOf(PART_1.copy(amount = Amount(-3000L)), PART_2))

    assertThat(transactionDiff(ID, saved, SPLIT))
      .isEqualTo(
        TransactionChanges(
          updated =
            listOf(
              TransactionUpdate(id = ID, error = Patch.To(null)),
              TransactionUpdate(id = PART_1.id, amount = Amount(-3210L)),
            ),
        ),
      )
  }

  @Test
  fun `Removed parts are deleted`() {
    val draft =
      SPLIT.copy(parts = listOf(PART_1.copy(amount = Amount(-4210L), category = CategoryId("b"))))

    assertThat(transactionDiff(ID, SPLIT, draft))
      .isEqualTo(
        TransactionChanges(
          updated =
            listOf(
              TransactionUpdate(
                id = PART_1.id,
                amount = Amount(-4210L),
                category = Patch.To(CategoryId("b")),
              ),
            ),
          deleted = listOf(PART_2.id),
        ),
      )
  }

  @Test
  fun `Removing every part stops it being a parent`() {
    val draft = TransactionDraft(SPLIT.fields.copy(category = CategoryId("a")))

    assertThat(transactionDiff(ID, SPLIT, draft))
      .isEqualTo(
        TransactionChanges(
          updated =
            listOf(
              TransactionUpdate(id = ID, isParent = false, category = Patch.To(CategoryId("a"))),
            ),
          deleted = listOf(PART_1.id, PART_2.id),
        ),
      )
  }

  // 'unlocking a reconciled split transaction propagates to children' upstream
  @Test
  fun `What changes on the parent reaches its parts`() {
    val saved = SPLIT.copy(fields = SPLIT.fields.copy(reconciled = true))
    val fields = SPLIT.fields.copy(date = LocalDate(2026, 10, 4), account = AccountId("b"))

    assertThat(transactionDiff(ID, saved, SPLIT.copy(fields = fields))?.updated)
      .isEqualTo(
        listOf(ID, PART_1.id, PART_2.id).map { id ->
          TransactionUpdate(
            id = id,
            account = AccountId("b"),
            date = LocalDate(2026, 10, 4),
            reconciled = false,
          )
        },
      )
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

    val PART_1 =
      SplitPart(TransactionId("p1"), Amount(-3210L), category = CategoryId("a"), sortOrder = -1)
    val PART_2 = SplitPart(TransactionId("p2"), Amount(-1000L), notes = "Bin bags", sortOrder = -2)
    val SPLIT = TransactionDraft(SAVED.copy(payee = null, category = null), listOf(PART_1, PART_2))
  }
}
