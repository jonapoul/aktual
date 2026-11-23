package aktual.budget.list.vm

import aktual.budget.model.BudgetId
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class BudgetNamesTest {
  @Test
  fun `Default name when nothing clashes`() {
    assertThat(uniqueBudgetName(existing = listOf("Other"))).isEqualTo("My Finances")
  }

  @Test
  fun `Append the first free index`() {
    val existing = listOf("My Finances", "My Finances 1")
    assertThat(uniqueBudgetName(existing)).isEqualTo("My Finances 2")
  }

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
  fun `Taken name suggests a free one`() {
    assertThat(validateBudgetName("Household ", existing = listOf("Household")))
      .isEqualTo(BudgetNameError.Taken("Household 1"))
  }

  @Test
  fun `ID from name`() {
    assertThat(budgetIdFromName("My Finances: 2", uuid = "0123456789"))
      .isEqualTo(BudgetId("My-Finances--2-0123456"))
  }
}
