package aktual.budget.navrail.ui

import aktual.core.nav.BudgetNavRailNavRoute
import aktual.core.nav.EditNavGridNavRoute
import aktual.core.nav.InfoNavRoute
import aktual.core.nav.ListBudgetsNavRoute
import aktual.core.nav.NavEntryContributor
import aktual.core.nav.NavStack
import aktual.core.nav.ServerUrlNavRoute
import aktual.core.nav.SettingsNavRoute
import aktual.core.ui.LoadingScreenIfNotNull
import aktual.core.ui.LocalBottomBarThemeAttrs
import aktual.di.AppScope
import aktual.di.RunLevelState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class BudgetNavRailNavEntryContributor(private val runLevelState: RunLevelState) :
  NavEntryContributor {
  override fun EntryProviderScope<NavKey>.contribute(stack: NavStack<NavKey>) {
    entry<BudgetNavRailNavRoute> {
      val themeAttrsStack = LocalBottomBarThemeAttrs.current
      DisposableEffect(themeAttrsStack) {
        themeAttrsStack.push(BudgetNavRailThemeAttrs)
        onDispose { themeAttrsStack.pop(BudgetNavRailThemeAttrs) }
      }

      val budgetGraph by remember { runLevelState.budget() }.collectAsState(initial = null)

      LoadingScreenIfNotNull(budgetGraph) {
        BudgetNavRail(
          onAction = { action ->
            when (action) {
              LogOut -> stack.replaceAll(ServerUrlNavRoute)
              SwitchFile -> stack.replaceAll(ListBudgetsNavRoute)
              Settings -> stack.push(SettingsNavRoute)
              About -> stack.push(InfoNavRoute)
              EditNavGrid -> stack.push(EditNavGridNavRoute)
            }
          }
        )
      }
    }
  }
}
