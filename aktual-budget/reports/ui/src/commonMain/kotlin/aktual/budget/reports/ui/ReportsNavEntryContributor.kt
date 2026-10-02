package aktual.budget.reports.ui

import aktual.budget.reports.ui.choosetype.ChooseReportTypeScreen
import aktual.budget.reports.ui.dashboard.ReportsDashboardScreen
import aktual.budget.reports.ui.report.ReportScreen
import aktual.budget.reports.ui.search.SearchReportsScreen
import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.CreateReportNavRoute
import aktual.core.nav.CreateReportNavigator
import aktual.core.nav.NavStack
import aktual.core.nav.ReportNavRoute
import aktual.core.nav.ReportNavigator
import aktual.core.nav.ReportsListNavRoute
import aktual.core.nav.SearchReportsNavRoute
import aktual.core.nav.SearchReportsNavigator
import aktual.core.nav.budgetEntry
import aktual.di.BudgetScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class ReportsNavEntryContributor : BudgetNavEntryContributor {
  override fun EntryProviderScope<BudgetNavKey>.contribute(
    stack: NavStack<BudgetNavKey>,
    appStack: NavStack<NavKey>,
  ) {
    budgetEntry<ReportsListNavRoute> {
      ReportsDashboardScreen(
        back = BackNavigator(stack),
        toReport = ReportNavigator(stack),
        toCreateReport = CreateReportNavigator(stack),
        toSearch = SearchReportsNavigator(stack),
      )
    }

    budgetEntry<SearchReportsNavRoute> {
      SearchReportsScreen(back = BackNavigator(stack), toReport = ReportNavigator(stack))
    }

    budgetEntry<ReportNavRoute> { route ->
      ReportScreen(id = route.id, back = BackNavigator(stack))
    }

    budgetEntry<CreateReportNavRoute> { route ->
      ChooseReportTypeScreen(
        page = route.page,
        back = BackNavigator(stack),
        toReport = ReportNavigator(stack),
      )
    }
  }
}
