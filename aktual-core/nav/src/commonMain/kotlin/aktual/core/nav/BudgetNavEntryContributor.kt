package aktual.core.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.EntryProviderScope

/**
 * Contributes nav entries from a budget feature module into the budget-scoped [EntryProviderScope].
 */
@Immutable
fun interface BudgetNavEntryContributor {
  fun EntryProviderScope<BudgetNavKey>.contribute(stack: NavStack<BudgetNavKey>)
}
