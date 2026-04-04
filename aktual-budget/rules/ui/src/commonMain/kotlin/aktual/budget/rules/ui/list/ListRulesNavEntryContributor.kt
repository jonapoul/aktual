package aktual.budget.rules.ui.list

import aktual.app.nav.BackNavigator
import aktual.app.nav.BudgetNavEntryContributor
import aktual.app.nav.BudgetNavKey
import aktual.app.nav.BudgetNavScope
import aktual.app.nav.ListRulesNavRoute
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetNavScope::class)
class ListRulesNavEntryContributor : BudgetNavEntryContributor {
  override fun contribute(
    scope: EntryProviderScope<BudgetNavKey>,
    stack: SnapshotStateList<BudgetNavKey>,
  ) {
    scope.entry<ListRulesNavRoute> { ListRulesScreen(BackNavigator(stack)) }
  }
}
