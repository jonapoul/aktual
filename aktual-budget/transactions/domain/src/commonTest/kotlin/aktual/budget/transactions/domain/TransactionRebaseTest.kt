package aktual.budget.transactions.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.ScheduleId
import aktual.budget.model.TransactionId
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class TransactionRebaseTest {
  @Test
  fun `An untouched draft follows what's saved`() {
    val saved = PLAIN.copy(fields = PLAIN.fields.copy(amount = Amount(-5000L)))

    assertThat(rebase(PLAIN, saved, PLAIN)).isEqualTo(RebasedDraft(saved, hasConflicts = false))
  }

  @Test
  fun `Fields changed in the draft keep its values and the rest follow what's saved`() {
    val saved = PLAIN.withFields(PLAIN.fields.copy(amount = Amount(-5000L), cleared = false))
    val draft = PLAIN.withFields(PLAIN.fields.copy(category = CategoryId("b"), notes = "Lunch"))

    assertThat(rebase(PLAIN, saved, draft))
      .isEqualTo(
        RebasedDraft(
          PLAIN.withFields(
            PLAIN.fields.copy(
              amount = Amount(-5000L),
              cleared = false,
              category = CategoryId("b"),
              notes = "Lunch",
            ),
          ),
          hasConflicts = false,
        ),
      )
    // So saving only writes what was changed in the draft
    assertThat(transactionDiff(ID, saved.fields, rebase(PLAIN, saved, draft).draft.fields))
      .isEqualTo(
        TransactionUpdate(
          id = ID,
          category = Patch.To(CategoryId("b")),
          notes = Patch.To("Lunch"),
        ),
      )
  }

  @Test
  fun `A field changed both ways keeps the draft's value as a conflict`() {
    val saved = PLAIN.withFields(PLAIN.fields.copy(payee = PayeeId("b")))
    val draft = PLAIN.withFields(PLAIN.fields.copy(payee = PayeeId("c")))

    assertThat(rebase(PLAIN, saved, draft)).isEqualTo(RebasedDraft(draft, hasConflicts = true))
  }

  @Test
  fun `A field changed the same way both ways isn't a conflict`() {
    val draft = PLAIN.withFields(PLAIN.fields.copy(payee = PayeeId("b")))

    assertThat(rebase(PLAIN, draft, draft)).isEqualTo(RebasedDraft(draft, hasConflicts = false))
  }

  @Test
  fun `A schedule linked elsewhere is kept when the draft didn't change it`() {
    val saved = PLAIN.withFields(PLAIN.fields.copy(schedule = ScheduleId("s")))
    val draft = PLAIN.withFields(PLAIN.fields.copy(notes = "Lunch"))

    assertThat(rebase(PLAIN, saved, draft))
      .isEqualTo(
        RebasedDraft(
          PLAIN.withFields(PLAIN.fields.copy(schedule = ScheduleId("s"), notes = "Lunch")),
          hasConflicts = false,
        ),
      )
  }

  @Test
  fun `Blank notes are the same as none`() {
    val base = PLAIN.withFields(PLAIN.fields.copy(notes = null))
    val saved = PLAIN.withFields(PLAIN.fields.copy(notes = "Elsewhere"))
    val draft = PLAIN.withFields(PLAIN.fields.copy(notes = ""))

    assertThat(rebase(base, saved, draft)).isEqualTo(RebasedDraft(saved, hasConflicts = false))
  }

  @Test
  fun `Parts are merged by ID`() {
    val part3 = SplitPart(TransactionId("p3"), Amount(-500L), sortOrder = -3)
    val saved = SPLIT.copy(parts = listOf(PART_1.copy(category = CategoryId("b")), PART_2, part3))
    val draft = SPLIT.copy(parts = listOf(PART_1.copy(notes = "Food"), PART_2))

    assertThat(rebase(SPLIT, saved, draft))
      .isEqualTo(
        RebasedDraft(
          SPLIT.copy(
            parts = listOf(PART_1.copy(category = CategoryId("b"), notes = "Food"), PART_2, part3),
          ),
          hasConflicts = false,
        ),
      )
  }

  @Test
  fun `A part deleted elsewhere is dropped`() {
    val saved = SPLIT.copy(parts = listOf(PART_1))

    val untouched = SPLIT.copy(parts = listOf(PART_1.copy(notes = "Food"), PART_2))
    assertThat(rebase(SPLIT, saved, untouched))
      .isEqualTo(
        RebasedDraft(SPLIT.copy(parts = listOf(PART_1.copy(notes = "Food"))), hasConflicts = false),
      )

    val changed = SPLIT.copy(parts = listOf(PART_1, PART_2.copy(notes = "Bags")))
    assertThat(rebase(SPLIT, saved, changed))
      .isEqualTo(RebasedDraft(SPLIT.copy(parts = listOf(PART_1)), hasConflicts = true))
  }

  @Test
  fun `Removing a part changed elsewhere is a conflict`() {
    val saved = SPLIT.copy(parts = listOf(PART_1, PART_2.copy(amount = Amount(-900L))))
    val draft = SPLIT.copy(parts = listOf(PART_1.copy(notes = "Food")))

    assertThat(rebase(SPLIT, saved, draft)).isEqualTo(RebasedDraft(draft, hasConflicts = true))
  }

  @Test
  fun `Parts added elsewhere aren't added to a draft that's no longer split`() {
    val part3 = SplitPart(TransactionId("p3"), Amount(-500L), sortOrder = -3)
    val saved = SPLIT.copy(parts = listOf(PART_1, PART_2, part3))
    val draft = TransactionDraft(SPLIT.fields)

    assertThat(rebase(SPLIT, saved, draft)).isEqualTo(RebasedDraft(draft, hasConflicts = true))
  }

  @Test
  fun `Parts added both ways keep the draft's as a conflict`() {
    val saved = PLAIN.copy(parts = listOf(PART_1, PART_2))
    val draft =
      PLAIN.copy(
        parts =
          listOf(
            SplitPart(TransactionId("p3"), Amount(-4000L), sortOrder = -1),
            SplitPart(TransactionId("p4"), Amount(-210L), sortOrder = -2),
          ),
      )

    assertThat(rebase(PLAIN, saved, draft)).isEqualTo(RebasedDraft(draft, hasConflicts = true))

    val part3 = SplitPart(TransactionId("p3"), Amount(-500L), sortOrder = -3)
    val part4 = SplitPart(TransactionId("p4"), Amount(-700L), sortOrder = -3)
    val withPart3 = SPLIT.copy(parts = SPLIT.parts + part3)
    val withPart4 = SPLIT.copy(parts = SPLIT.parts + part4)
    assertThat(rebase(SPLIT, withPart3, withPart4))
      .isEqualTo(RebasedDraft(withPart4, hasConflicts = true))
  }

  @Test
  fun `A split that stops adding up keeps the draft's amounts as a conflict`() {
    val total = FIELDS.copy(payee = null, category = null, amount = Amount(-5000L))
    val saved = TransactionDraft(total, listOf(PART_1.copy(amount = Amount(-4000L)), PART_2))
    val draft = TransactionDraft(total, listOf(PART_1, PART_2.copy(amount = Amount(-1790L))))

    assertThat(rebase(SPLIT, saved, draft)).isEqualTo(RebasedDraft(draft, hasConflicts = true))
  }

  private companion object {
    val ID = TransactionId("t")

    val FIELDS =
      TransactionFields(
        account = AccountId("a"),
        date = LocalDate(2026, 10, 3),
        amount = Amount(-4210L),
        payee = PayeeId("a"),
        category = CategoryId("a"),
        notes = null,
        cleared = true,
        reconciled = false,
      )

    val PLAIN = TransactionDraft(FIELDS)

    val PART_1 =
      SplitPart(TransactionId("p1"), Amount(-3210L), category = CategoryId("a"), sortOrder = -1)
    val PART_2 = SplitPart(TransactionId("p2"), Amount(-1000L), notes = "Bin bags", sortOrder = -2)
    val SPLIT = TransactionDraft(FIELDS.copy(payee = null, category = null), listOf(PART_1, PART_2))
  }
}
