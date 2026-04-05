package aktual.budget.navrail.ui

import aktual.app.nav.BackNavigator
import aktual.app.nav.BudgetNavRailNavRoute
import aktual.app.nav.NavEntryContributor
import aktual.app.nav.NavScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class BudgetNavRailNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: SnapshotStateList<NavKey>) {
    scope.entry<BudgetNavRailNavRoute> { route ->
      BudgetNavRail(back = BackNavigator(stack), token = route.token, budgetId = route.budgetId)
    }
  }
}
