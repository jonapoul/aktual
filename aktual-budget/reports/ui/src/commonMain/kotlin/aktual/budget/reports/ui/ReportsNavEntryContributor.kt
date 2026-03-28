package aktual.budget.reports.ui

import aktual.app.nav.BackNavigator
import aktual.app.nav.BudgetNavEntryContributor
import aktual.app.nav.BudgetNavKey
import aktual.app.nav.BudgetNavScope
import aktual.app.nav.CreateReportNavRoute
import aktual.app.nav.CreateReportNavigator
import aktual.app.nav.ReportNavRoute
import aktual.app.nav.ReportNavigator
import aktual.app.nav.ReportsListNavRoute
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetNavScope::class)
class ReportsNavEntryContributor : BudgetNavEntryContributor {
  override fun contribute(
    scope: EntryProviderScope<BudgetNavKey>,
    stack: SnapshotStateList<BudgetNavKey>,
  ) {
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
