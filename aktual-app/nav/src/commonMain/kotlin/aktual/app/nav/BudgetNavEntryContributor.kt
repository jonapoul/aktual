package aktual.app.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.EntryProviderScope

/**
 * Contributes nav entries from a budget feature module into the budget-scoped [EntryProviderScope].
 */
@Immutable
fun interface BudgetNavEntryContributor {
  fun contribute(scope: EntryProviderScope<BudgetNavKey>, stack: AktualNavStack<BudgetNavKey>)
}
