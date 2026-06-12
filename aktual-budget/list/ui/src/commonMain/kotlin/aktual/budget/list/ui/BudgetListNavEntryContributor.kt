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
import aktual.core.ui.LoadingScreenIfNotNull
import aktual.di.AppScope
import aktual.di.RunLevelState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class BudgetListNavEntryContributor(private val runLevelState: RunLevelState) :
  NavEntryContributor {
  override fun EntryProviderScope<NavKey>.contribute(stack: NavStack<NavKey>) {
    entry<ListBudgetsNavRoute> {
      val loggedInGraph by remember { runLevelState.loggedIn() }.collectAsState(initial = null)

      LoadingScreenIfNotNull(loggedInGraph) {
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
}
