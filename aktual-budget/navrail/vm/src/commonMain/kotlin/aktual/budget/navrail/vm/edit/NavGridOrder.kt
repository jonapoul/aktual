package aktual.budget.navrail.vm.edit

import aktual.core.nav.BudgetTab
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal val DEFAULT_NAV_GRID_ORDER: ImmutableList<BudgetTab> = BudgetTab.entries.toImmutableList()

// Maps persisted tab names back to BudgetTab, dropping any unknown names and appending any tabs
// that aren't in the stored order. This keeps old/new app versions compatible when entries change
internal fun reconcileNavGridOrder(names: List<String>): ImmutableList<BudgetTab> {
  val byName = BudgetTab.entries.associateBy { it.name }
  val ordered = names.mapNotNull { byName[it] }
  val missing = BudgetTab.entries.filter { it !in ordered }
  return (ordered + missing).toImmutableList()
}
