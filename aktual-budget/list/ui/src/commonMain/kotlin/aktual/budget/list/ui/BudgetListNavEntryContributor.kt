package aktual.budget.list.ui

import aktual.app.nav.ChangePasswordNavRoute
import aktual.app.nav.InfoNavRoute
import aktual.app.nav.ListBudgetsNavRoute
import aktual.app.nav.LoginNavRoute
import aktual.app.nav.MetricsNavRoute
import aktual.app.nav.NavEntryContributor
import aktual.app.nav.NavScope
import aktual.app.nav.ServerUrlNavRoute
import aktual.app.nav.SettingsNavRoute
import aktual.app.nav.TransactionsNavRoute
import aktual.app.nav.debugPopUpToAndPush
import aktual.app.nav.debugPush
import aktual.budget.model.BudgetId
import aktual.core.model.Token
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class BudgetListNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: SnapshotStateList<NavKey>) {
    scope.entry<ListBudgetsNavRoute> { route ->
      ListBudgetsScreen(ListBudgetsNavigatorImpl(stack), route.token)
    }
  }
}

private class ListBudgetsNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ListBudgetsNavigator {
  override fun toAbout() = stack.debugPush(InfoNavRoute)

  override fun toChangePassword() = stack.debugPush(ChangePasswordNavRoute)

  override fun toMetrics() = stack.debugPush(MetricsNavRoute)

  override fun toSettings() = stack.debugPush(SettingsNavRoute)

  override fun toBudget(token: Token, budgetId: BudgetId) =
    stack.debugPopUpToAndPush(
      TransactionsNavRoute(token, budgetId),
      { it is ListBudgetsNavRoute },
      inclusive = false,
    )

  override fun toUrl() =
    stack.debugPopUpToAndPush(ServerUrlNavRoute, { it is LoginNavRoute }, inclusive = true)
}
