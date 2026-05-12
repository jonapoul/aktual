package aktual.budget.reports.ui

import aktual.budget.reports.ui.choosetype.ChooseReportTypeScreen
import aktual.budget.reports.ui.dashboard.ReportsDashboardScreen
import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.CreateReportNavRoute
import aktual.core.nav.CreateReportNavigator
import aktual.core.nav.NavStack
import aktual.core.nav.ReportNavRoute
import aktual.core.nav.ReportNavigator
import aktual.core.nav.ReportsListNavRoute
import aktual.core.nav.budgetEntry
import aktual.di.BudgetScope
import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class ReportsNavEntryContributor : BudgetNavEntryContributor {
  override fun contribute(scope: EntryProviderScope<BudgetNavKey>, stack: NavStack<BudgetNavKey>) {
    scope.budgetEntry<ReportsListNavRoute> {
      ReportsDashboardScreen(
        back = BackNavigator(stack),
        toReport = ReportNavigator(stack),
        toCreateReport = CreateReportNavigator(stack),
      )
    }

    scope.budgetEntry<ReportNavRoute> {
      // TBC
    }

    scope.budgetEntry<CreateReportNavRoute> {
      ChooseReportTypeScreen(back = BackNavigator(stack), toReport = ReportNavigator(stack))
    }
  }
}
