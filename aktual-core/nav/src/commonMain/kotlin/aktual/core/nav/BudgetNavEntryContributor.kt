package aktual.core.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/**
 * Contributes nav entries from a budget feature module into the budget-scoped [EntryProviderScope].
 * [stack] is the active tab's stack, [appStack] the app-level one, for pushing screens outside the
 * budget nav rail (e.g. settings).
 */
@Immutable
fun interface BudgetNavEntryContributor {
  fun EntryProviderScope<BudgetNavKey>.contribute(
    stack: NavStack<BudgetNavKey>,
    appStack: NavStack<NavKey>,
  )
}
