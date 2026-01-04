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
import aktual.core.model.Token
import aktual.metrics.ui.MetricsScreen
import aktual.settings.ui.SettingsScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AktualNavHost(
  nav: NavHostController,
  isServerUrlSet: Boolean,
  token: Token?,
  modifier: Modifier = Modifier,
) {
  NavHost(
    modifier = modifier,
    navController = nav,
    startDestination = when {
      token != null && isServerUrlSet -> ListBudgetsNavRoute(token)
      isServerUrlSet -> LoginNavRoute
      else -> ServerUrlNavRoute
    },
  ) {
    composable<ChangePasswordNavRoute> { ChangePasswordScreen(ChangePasswordNavigator(nav)) }

    composable<InfoNavRoute> { InfoScreen(InfoNavigator(nav)) }

    composable<LicensesNavRoute> { LicensesScreen(LicensesNavigator(nav)) }

    composable<MetricsNavRoute> { MetricsScreen(MetricsNavigator(nav)) }

    composableWithArg<ListBudgetsNavRoute>(mapOf(TokenType)) { route, _ ->
      ListBudgetsScreen(ListBudgetsNavigator(nav), route.token)
    }

    composable<LoginNavRoute> { LoginScreen(LoginNavigator(nav)) }

    composableWithArg<ReportsListNavRoute>(mapOf(BudgetIdType, TokenType)) { route, _ ->
      ReportsDashboardScreen(ReportsDashboardNavigator(nav), route.budgetId, route.token)
    }

    composableWithArg<ReportNavRoute>(mapOf(BudgetIdType, TokenType, WidgetIdType)) { route, _ ->
      // TBC
    }

    composableWithArg<CreateReportNavRoute>(mapOf(BudgetIdType, TokenType)) { route, _ ->
      ChooseReportTypeScreen(ChooseReportTypeNavigator(nav), route.budgetId, route.token)
    }

    composable<ServerUrlNavRoute> { ServerUrlScreen(ServerUrlNavigator(nav)) }

    composable<SettingsNavRoute> { SettingsScreen(SettingsNavigator(nav)) }

    composableWithArg<SyncBudgetsNavRoute>(mapOf(BudgetIdType, TokenType)) { route, _ ->
      SyncBudgetScreen(SyncBudgetNavigator(nav), route.budgetId, route.token)
    }

    composableWithArg<TransactionsNavRoute>(mapOf(BudgetIdType, TokenType)) { route, _ ->
      TransactionsScreen(TransactionsNavigator(nav), route.budgetId, route.token)
    }
  }
}
