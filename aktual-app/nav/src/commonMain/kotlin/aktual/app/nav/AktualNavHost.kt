package aktual.app.nav

import aktual.about.ui.info.InfoScreen
import aktual.about.ui.licenses.LicensesScreen
import aktual.account.ui.login.LoginScreen
import aktual.account.ui.password.ChangePasswordScreen
import aktual.account.ui.url.ServerUrlScreen
import aktual.budget.list.ui.ListBudgetsScreen
import aktual.budget.reports.ui.ChooseReportTypeScreen
import aktual.budget.reports.ui.ReportsDashboardScreen
import aktual.budget.sync.ui.SyncBudgetScreen
import aktual.budget.transactions.ui.TransactionsScreen
import aktual.metrics.ui.MetricsScreen
import aktual.settings.ui.inspect.InspectThemeScreen
import aktual.settings.ui.root.SettingsScreen
import aktual.settings.ui.theme.ThemeSettingsScreen
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

@Composable
fun AktualNavHost(backStack: SnapshotStateList<NavKey>, modifier: Modifier = Modifier) {
  NavDisplay(
    backStack = backStack,
    modifier = modifier,
    onBack = { backStack.debugPop() },
    // Forward: new screen slides in from right, old screen slides out to left
    transitionSpec = {
      slideInHorizontally(initialOffsetX = { width -> width }) togetherWith
        slideOutHorizontally(targetOffsetX = { width -> -width })
    },
    // Back: previous screen slides in from left, current screen slides out to right
    popTransitionSpec = {
      slideInHorizontally(initialOffsetX = { width -> -width }) togetherWith
        slideOutHorizontally(targetOffsetX = { width -> width })
    },
    // Predictive back gesture: same as pop
    predictivePopTransitionSpec = {
      slideInHorizontally(initialOffsetX = { width -> -width }) togetherWith
        slideOutHorizontally(targetOffsetX = { width -> width })
    },
    entryDecorators =
      listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
    entryProvider =
      entryProvider {
        entry<ChangePasswordNavRoute> {
          ChangePasswordScreen(ChangePasswordNavigatorImpl(backStack))
        }

        entry<InfoNavRoute> { InfoScreen(InfoNavigatorImpl(backStack)) }

        entry<LicensesNavRoute> { LicensesScreen(LicensesNavigatorImpl(backStack)) }

        entry<MetricsNavRoute> { MetricsScreen(MetricsNavigatorImpl(backStack)) }

        entry<ListBudgetsNavRoute> { route ->
          ListBudgetsScreen(ListBudgetsNavigatorImpl(backStack), route.token)
        }

        entry<LoginNavRoute> { LoginScreen(LoginNavigatorImpl(backStack)) }

        entry<ReportsListNavRoute> { route ->
          ReportsDashboardScreen(
            ReportsDashboardNavigatorImpl(backStack),
            route.budgetId,
            route.token,
          )
        }

        entry<ReportNavRoute> {
          // TBC
        }

        entry<CreateReportNavRoute> { route ->
          ChooseReportTypeScreen(
            ChooseReportTypeNavigatorImpl(backStack),
            route.budgetId,
            route.token,
          )
        }

        entry<ServerUrlNavRoute> { ServerUrlScreen(ServerUrlNavigatorImpl(backStack)) }

        entry<SettingsNavRoute> { SettingsScreen(SettingsNavigatorImpl(backStack)) }

        entry<ThemeSettingsNavRoute> { ThemeSettingsScreen(ThemeSettingsNavigatorImpl(backStack)) }

        entry<InspectThemeNavRoute> { route ->
          InspectThemeScreen(InspectThemeNavigatorImpl(backStack), route.id)
        }

        entry<SyncBudgetsNavRoute> { route ->
          SyncBudgetScreen(SyncBudgetNavigatorImpl(backStack), route.budgetId, route.token)
        }

        entry<TransactionsNavRoute> { route ->
          TransactionsScreen(TransactionsNavigatorImpl(backStack), route.budgetId, route.token)
        }
      },
  )
}
