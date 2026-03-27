package aktual.budget.reports.ui

import aktual.app.nav.BackNavigator
import aktual.app.nav.CreateReportNavRoute
import aktual.app.nav.CreateReportNavigator
import aktual.app.nav.NavEntryContributor
import aktual.app.nav.NavScope
import aktual.app.nav.ReportNavRoute
import aktual.app.nav.ReportNavigator
import aktual.app.nav.ReportsListNavRoute
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class ReportsNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: SnapshotStateList<NavKey>) {
    scope.entry<ReportsListNavRoute> { route ->
      ReportsDashboardScreen(
        back = BackNavigator(stack),
        toReport = ReportNavigator(stack),
        toCreateReport = CreateReportNavigator(stack),
        budgetId = route.budgetId,
        token = route.token,
      )
    }

    scope.entry<ReportNavRoute> {
      // TBC
    }

    scope.entry<CreateReportNavRoute> { route ->
      ChooseReportTypeScreen(
        back = BackNavigator(stack),
        toReport = ReportNavigator(stack),
        budgetId = route.budgetId,
        token = route.token,
      )
    }
  }
}
