package aktual.budget.list.ui

import aktual.app.nav.AktualNavStack
import aktual.app.nav.BudgetNavRailNavigator
import aktual.app.nav.ChangePasswordNavigator
import aktual.app.nav.InfoNavigator
import aktual.app.nav.ListBudgetsNavRoute
import aktual.app.nav.MetricsNavigator
import aktual.app.nav.NavEntryContributor
import aktual.app.nav.NavScope
import aktual.app.nav.ServerUrlNavigator
import aktual.app.nav.SettingsNavigator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class BudgetListNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: AktualNavStack<NavKey>) {
    scope.entry<ListBudgetsNavRoute> { route ->
      ListBudgetsScreen(
        token = route.token,
        toInfo = InfoNavigator(stack),
        toChangePassword = ChangePasswordNavigator(stack),
        toSettings = SettingsNavigator(stack),
        toMetrics = MetricsNavigator(stack),
        logOut = ServerUrlNavigator(stack),
        toBudget = BudgetNavRailNavigator(stack),
      )
    }
  }
}
