package aktual.budget.navrail.ui

import aktual.app.nav.AktualNavStack
import aktual.app.nav.BudgetNavRailNavRoute
import aktual.app.nav.InfoNavRoute
import aktual.app.nav.ListBudgetsNavRoute
import aktual.app.nav.NavEntryContributor
import aktual.app.nav.NavScope
import aktual.app.nav.ServerUrlNavRoute
import aktual.app.nav.SettingsNavRoute
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class BudgetNavRailNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: AktualNavStack<NavKey>) {
    scope.entry<BudgetNavRailNavRoute> { route ->
      BudgetNavRail(
        token = route.token,
        budgetId = route.budgetId,
        onAction = { action ->
          when (action) {
            BudgetNavAction.LogOut -> stack.replaceAll(ServerUrlNavRoute)
            BudgetNavAction.SwitchFile -> stack.replaceAll(ListBudgetsNavRoute(route.token))
            BudgetNavAction.Settings -> stack.push(SettingsNavRoute)
            BudgetNavAction.About -> stack.push(InfoNavRoute)
          }
        },
      )
    }
  }
}
