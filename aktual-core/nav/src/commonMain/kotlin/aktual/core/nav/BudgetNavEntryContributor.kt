package aktual.core.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey

/**
 * Contributes nav entries from a budget feature module into the budget-scoped [BudgetEntryScope].
 * stack is the active tab's stack, appStack the app-level one, for pushing screens outside the
 * budget nav rail (e.g. settings).
 */
@Immutable
fun interface BudgetNavEntryContributor {
  fun BudgetEntryScope.contribute(
    stack: NavStack<BudgetNavKey>,
    appStack: NavStack<NavKey>,
  )
}
