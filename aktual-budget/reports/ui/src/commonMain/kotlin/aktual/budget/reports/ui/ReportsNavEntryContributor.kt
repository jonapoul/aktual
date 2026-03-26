package aktual.budget.reports.ui

import aktual.app.nav.CreateReportNavRoute
import aktual.app.nav.NavEntryContributor
import aktual.app.nav.NavScope
import aktual.app.nav.ReportNavRoute
import aktual.app.nav.ReportsListNavRoute
import aktual.app.nav.debugPop
import aktual.app.nav.debugPush
import aktual.budget.model.BudgetId
import aktual.budget.model.WidgetId
import aktual.core.model.Token
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class ReportsNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: SnapshotStateList<NavKey>) {
    scope.entry<ReportsListNavRoute> { route ->
      ReportsDashboardScreen(ReportsDashboardNavigatorImpl(stack), route.budgetId, route.token)
    }

    scope.entry<ReportNavRoute> {
      // TBC
    }

    scope.entry<CreateReportNavRoute> { route ->
      ChooseReportTypeScreen(ChooseReportTypeNavigatorImpl(stack), route.budgetId, route.token)
    }
  }
}

private class ReportsDashboardNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ReportsDashboardNavigator {
  override fun back() = stack.debugPop()

  override fun toReport(token: Token, budgetId: BudgetId, widgetId: WidgetId) =
    stack.debugPush(ReportNavRoute(token, budgetId, widgetId))

  override fun createReport(token: Token, budgetId: BudgetId) =
    stack.debugPush(CreateReportNavRoute(token, budgetId))
}

private class ChooseReportTypeNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ChooseReportTypeNavigator {
  override fun back() = stack.debugPop()

  override fun toReport(token: Token, budgetId: BudgetId, widgetId: WidgetId) =
    stack.debugPush(ReportNavRoute(token, budgetId, widgetId))
}
