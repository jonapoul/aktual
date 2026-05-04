package aktual.budget.list.ui

import aktual.core.nav.BudgetNavRailNavigator
import aktual.core.nav.ChangePasswordNavigator
import aktual.core.nav.InfoNavigator
import aktual.core.nav.ListBudgetsNavRoute
import aktual.core.nav.MetricsNavigator
import aktual.core.nav.NavEntryContributor
import aktual.core.nav.NavStack
import aktual.core.nav.ServerUrlNavigator
import aktual.core.nav.SettingsNavigator
import aktual.di.AppScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class BudgetListNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: NavStack<NavKey>) {
    scope.entry<ListBudgetsNavRoute> { route ->
      ListBudgetsScreen(
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
