package aktual.budget.navrail.vm.edit

import aktual.core.nav.BudgetTab
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal val DEFAULT_NAV_GRID_ORDER: ImmutableList<BudgetTab> = BudgetTab.entries.toImmutableList()

// Maps persisted tab names back to BudgetTab, dropping any unknown names and slotting in any tabs
// that aren't in the stored order. This keeps old/new app versions compatible when entries change.
// Missing tabs slot in after the existing tabs (before any actions); missing actions go at the end,
// so a newly-added tab never lands after Settings/Log out etc
internal fun reconcileNavGridOrder(names: List<String>): ImmutableList<BudgetTab> {
  val byName = BudgetTab.entries.associateBy { it.name }
  val ordered = names.mapNotNull { byName[it] }.toMutableList()
  val missing = BudgetTab.entries.filter { it !in ordered }
  for (tab in missing) {
    when (tab.category) {
      BudgetTab.Category.Tab -> {
        val insertAt = ordered.indexOfLast { it.category == BudgetTab.Category.Tab } + 1
        ordered.add(insertAt, tab)
      }
      BudgetTab.Category.Action -> ordered.add(tab)
    }
  }
  return ordered.toImmutableList()
}
