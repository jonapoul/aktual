package aktual.budget.list.vm

import aktual.budget.model.BudgetId
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class BudgetNamesTest {
  @Test
  fun `Valid name`() {
    assertThat(validateBudgetName("  Household ", existing = listOf("Other"))).isNull()
  }

  @Test
  fun `Blank name`() {
    assertThat(validateBudgetName("   ", existing = emptyList())).isEqualTo(Blank)
  }

  @Test
  fun `Too long name`() {
    val name = "a".repeat(MAX_BUDGET_NAME_LENGTH + 1)
    assertThat(validateBudgetName(name, existing = emptyList())).isEqualTo(TooLong)
  }

  @Test
  fun `Taken name`() {
    assertThat(validateBudgetName("Household ", existing = listOf("Household")))
      .isEqualTo(BudgetNameError.Taken("Household"))
  }

  @Test
  fun `ID from name`() {
    assertThat(budgetIdFromName("My Finances: 2", uuid = "0123456789"))
      .isEqualTo(BudgetId("My-Finances--2-0123456"))
  }
}
