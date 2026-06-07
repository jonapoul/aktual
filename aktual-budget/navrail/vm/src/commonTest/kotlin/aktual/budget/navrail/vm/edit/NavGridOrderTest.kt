package aktual.budget.navrail.vm.edit

import aktual.core.nav.BudgetTab
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test

class NavGridOrderTest {
  @Test
  fun `empty names yields default order`() {
    assertThat(reconcileNavGridOrder(emptyList()))
      .containsExactly(*BudgetTab.entries.toTypedArray())
  }

  @Test
  fun `unknown names are dropped and missing tabs appended in enum order`() {
    val result = reconcileNavGridOrder(listOf("Reports", "Bogus", "Transactions"))
    assertThat(result)
      .containsExactly(
        BudgetTab.Reports,
        BudgetTab.Transactions,
        BudgetTab.Schedules,
        BudgetTab.Rules,
        BudgetTab.Tags,
        BudgetTab.SwitchBudget,
        BudgetTab.LogOut,
        BudgetTab.Settings,
        BudgetTab.About,
      )
  }

  @Test
  fun `a fully specified custom order is preserved exactly`() {
    val custom =
      listOf(
        BudgetTab.About,
        BudgetTab.Rules,
        BudgetTab.Transactions,
        BudgetTab.LogOut,
        BudgetTab.Tags,
        BudgetTab.Reports,
        BudgetTab.Settings,
        BudgetTab.Schedules,
        BudgetTab.SwitchBudget,
      )
    assertThat(reconcileNavGridOrder(custom.map { it.name }))
      .containsExactly(*custom.toTypedArray())
  }
}
