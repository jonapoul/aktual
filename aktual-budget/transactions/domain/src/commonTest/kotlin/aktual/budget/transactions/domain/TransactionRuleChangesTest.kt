package aktual.budget.transactions.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class TransactionRuleChangesTest {
  @Test
  fun `Rules fill empty fields`() {
    val after = DRAFT.copy(category = CategoryId("groceries"), notes = "weekly", cleared = true)

    assertThat(applyRuleChanges(DRAFT, after, TransactionField.Amount, emptySet())).isEqualTo(after)
  }

  @Test
  fun `Rules leave filled fields alone`() {
    val draft = DRAFT.copy(category = CategoryId("fuel"), amount = Amount(-1000L))
    val after =
      draft.copy(
        category = CategoryId("groceries"),
        amount = Amount(-2000L),
        account = AccountId("savings"),
        date = LocalDate(2026, 1, 1),
      )

    assertThat(applyRuleChanges(draft, after, Notes, emptySet())).isEqualTo(draft)
  }

  @Test
  fun `A payee change applies everything`() {
    val draft = DRAFT.copy(category = CategoryId("fuel"), notes = "mine")
    val after = draft.copy(category = CategoryId("groceries"), notes = "theirs")

    assertThat(applyRuleChanges(draft, after, Payee, emptySet())).isEqualTo(after)
  }

  @Test
  fun `Fields the user emptied stay empty`() {
    val after = DRAFT.copy(category = CategoryId("groceries"), notes = "weekly")
    val emptied = setOf(TransactionField.Category)

    assertThat(applyRuleChanges(DRAFT, after, Payee, emptied))
      .isEqualTo(DRAFT.copy(notes = "weekly"))
  }

  @Test
  fun `Rules can add to a note`() {
    val draft = DRAFT.copy(notes = "milk")
    val after = draft.copy(notes = "#food milk (shop)")

    assertThat(applyRuleChanges(draft, after, TransactionField.Amount, emptySet())).isEqualTo(after)
  }

  @Test
  fun `Rules don't add to a note twice`() {
    val draft = DRAFT.copy(notes = "#food milk")
    val after = draft.copy(notes = "#food #food milk")

    assertThat(applyRuleChanges(draft, after, TransactionField.Amount, emptySet())).isEqualTo(draft)
  }

  @Test
  fun `Rules don't replace a note`() {
    val draft = DRAFT.copy(notes = "milk")
    val after = draft.copy(notes = "groceries")

    assertThat(applyRuleChanges(draft, after, TransactionField.Amount, emptySet())).isEqualTo(draft)
  }

  @Test
  fun `Zero, unticked and missing values are empty`() {
    val filled = DRAFT.copy(amount = Amount(-1L), category = CategoryId("fuel"), cleared = true)

    assertThat(TransactionField.entries.filter(DRAFT::isEmpty))
      .isEqualTo(
        listOf(
          TransactionField.Amount,
          TransactionField.Category,
          TransactionField.Notes,
          TransactionField.Cleared,
        ),
      )
    assertThat(TransactionField.entries.filter(filled::isEmpty)).isEqualTo(listOf(Notes))
  }

  private companion object {
    val DRAFT =
      TransactionFields(
        account = AccountId("current"),
        date = LocalDate(2026, 10, 3),
        amount = Zero,
        payee = PayeeId("tesco"),
        category = null,
        notes = null,
        cleared = false,
        reconciled = false,
      )
  }
}
