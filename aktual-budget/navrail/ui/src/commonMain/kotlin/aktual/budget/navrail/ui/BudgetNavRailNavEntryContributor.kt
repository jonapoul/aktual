package aktual.budget.navrail.ui

import aktual.core.nav.BudgetNavRailNavRoute
import aktual.core.nav.InfoNavRoute
import aktual.core.nav.ListBudgetsNavRoute
import aktual.core.nav.NavEntryContributor
import aktual.core.nav.NavStack
import aktual.core.nav.ServerUrlNavRoute
import aktual.core.nav.SettingsNavRoute
import aktual.core.ui.LocalBottomBarThemeAttrs
import aktual.di.AppScope
import androidx.compose.runtime.DisposableEffect
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class BudgetNavRailNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: NavStack<NavKey>) {
    scope.entry<BudgetNavRailNavRoute> {
      val themeAttrsStack = LocalBottomBarThemeAttrs.current
      DisposableEffect(themeAttrsStack) {
        themeAttrsStack.push(BudgetNavRailThemeAttrs)
        onDispose { themeAttrsStack.pop(BudgetNavRailThemeAttrs) }
      }

      BudgetNavRail(
        onAction = { action ->
          when (action) {
            BudgetNavAction.LogOut -> stack.replaceAll(ServerUrlNavRoute)
            BudgetNavAction.SwitchFile -> stack.replaceAll(ListBudgetsNavRoute)
            BudgetNavAction.Settings -> stack.push(SettingsNavRoute)
            BudgetNavAction.About -> stack.push(InfoNavRoute)
          }
        }
      )
    }
  }
}
