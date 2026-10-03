package aktual.budget.navrail.ui

import aktual.budget.demo.DemoBudget
import aktual.core.nav.BudgetNavRailNavRoute
import aktual.core.nav.InfoNavRoute
import aktual.core.nav.ListBudgetsNavRoute
import aktual.core.nav.NavEntryContributor
import aktual.core.nav.NavStack
import aktual.core.nav.ServerUrlNavRoute
import aktual.core.nav.SettingsNavRoute
import aktual.core.ui.LoadingScreenIfNull
import aktual.di.AppCoroutineScope
import aktual.di.AppScope
import aktual.di.RunLevelState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet
import kotlinx.coroutines.launch

@ContributesIntoSet(AppScope::class)
class BudgetNavRailNavEntryContributor(
  private val runLevelState: RunLevelState,
  private val demoBudget: DemoBudget,
  private val scope: AppCoroutineScope,
) : NavEntryContributor {
  override fun EntryProviderScope<NavKey>.contribute(stack: NavStack<NavKey>) {
    entry<BudgetNavRailNavRoute> {
      val budgetGraph by remember { runLevelState.budget() }.collectAsState(initial = null)

      LoadingScreenIfNull(budgetGraph) {
        BudgetNavRail(
          appStack = stack,
          onAction = { action ->
            when (action) {
              LogOut -> stack.replaceAll(ServerUrlNavRoute)
              SwitchFile -> stack.replaceAll(ListBudgetsNavRoute)
              ExitDemo -> exitDemo(stack)
              Settings -> stack.push(SettingsNavRoute)
              About -> stack.push(InfoNavRoute)
            }
          },
        )
      }
    }
  }

  private fun exitDemo(stack: NavStack<NavKey>) {
    scope.launch { demoBudget.close() }
    stack.replaceAll(ServerUrlNavRoute)
  }
}
