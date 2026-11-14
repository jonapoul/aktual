package aktual.budget.rules.vm.edit

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.budget.rules.domain.RuleTransaction
import aktual.budget.transactions.domain.RuleMatch
import aktual.budget.transactions.domain.RuleMatches
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class RuleMatchesStateTest {
  private val before =
    RuleTransaction(
      id = TransactionId("t"),
      account = AccountId("a"),
      date = LocalDate(2026, 1, 1),
      amount = Amount(-1000),
      payee = PayeeId("tesco"),
      importedPayee = "TESCO 123",
    )

  private val names = mapOf("a" to "Current", "tesco" to "Tesco", "groceries" to "Groceries")

  @Test
  fun `Rows list what the actions change`() {
    val after = before.copy(category = CategoryId("groceries"), notes = "#food", cleared = false)
    val state = matches(RuleMatch(before, after), RuleMatch(before, before)).toState()

    assertThat(state).all {
      prop(RuleMatchesState.Loaded::total).isEqualTo(2)
      prop(RuleMatchesState.Loaded::changing).isEqualTo(1)
    }
    assertThat(state.rows[0]).all {
      prop(MatchRow::payee).isEqualTo("Tesco")
      prop(MatchRow::changes)
        .containsExactly(
          MatchChange.Named(Category, null, "Groceries"),
          MatchChange.Named(Notes, null, "#food"),
          MatchChange.Cleared(false),
        )
    }
    assertThat(state.rows[1].changes).isEqualTo(emptyList<MatchChange>())
  }

  @Test
  fun `A deleted transaction shows nothing else`() {
    val after = before.copy(tombstone = true, notes = "gone")
    val state = matches(RuleMatch(before, after)).toState()
    assertThat(state.rows[0].changes).containsExactly(MatchChange.Deleted)
  }

  private fun matches(vararg matches: RuleMatch) = RuleMatches(matches.toList(), names)
}
