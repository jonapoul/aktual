package actual.android.app.nav

import actual.about.ui.info.InfoScreen
import actual.about.ui.licenses.LicensesScreen
import actual.account.model.LoginToken
import actual.account.ui.login.LoginScreen
import actual.account.ui.password.ChangePasswordScreen
import actual.account.ui.url.ServerUrlScreen
import actual.budget.list.ui.ListBudgetsScreen
import actual.budget.reports.ui.ChooseReportTypeScreen
import actual.budget.reports.ui.ReportsDashboardScreen
import actual.budget.sync.ui.SyncBudgetScreen
import actual.budget.transactions.ui.TransactionsScreen
import actual.settings.ui.SettingsScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
internal fun ActualNavHost(
  isServerUrlSet: Boolean,
  loginToken: LoginToken?,
  modifier: Modifier = Modifier,
  nav: NavHostController = rememberNavController(),
) {
  NavHost(
    modifier = modifier,
    navController = nav,
    startDestination = when {
      loginToken != null && isServerUrlSet -> ListBudgetsNavRoute(loginToken)
      isServerUrlSet -> LoginNavRoute
      else -> ServerUrlNavRoute
    },
  ) {
    composable<ChangePasswordNavRoute> { ChangePasswordScreen(ChangePasswordNavigator(nav)) }

    composable<InfoNavRoute> { InfoScreen(InfoNavigator(nav)) }

    composable<LicensesNavRoute> { LicensesScreen(LicensesNavigator(nav)) }

    composableWithArg<ListBudgetsNavRoute>(mapOf(LoginTokenType)) { route, _ ->
      ListBudgetsScreen(ListBudgetsNavigator(nav), route.token)
    }

    composable<LoginNavRoute> { LoginScreen(LoginNavigator(nav)) }

    composableWithArg<ReportsListNavRoute>(mapOf(BudgetIdType, LoginTokenType)) { route, _ ->
      ReportsDashboardScreen(ReportsDashboardNavigator(nav), route.budgetId, route.token)
    }

    composableWithArg<ReportNavRoute>(mapOf(BudgetIdType, LoginTokenType, CustomReportIdType)) { route, _ ->
      // TBC
    }

    composableWithArg<CreateReportNavRoute>(mapOf(BudgetIdType, LoginTokenType)) { route, _ ->
      ChooseReportTypeScreen(ChooseReportTypeNavigator(nav), route.budgetId, route.token)
    }

    composable<ServerUrlNavRoute> { ServerUrlScreen(ServerUrlNavigator(nav)) }

    composable<SettingsNavRoute> { SettingsScreen(SettingsNavigator(nav)) }

    composableWithArg<SyncBudgetsNavRoute>(mapOf(BudgetIdType, LoginTokenType)) { route, _ ->
      SyncBudgetScreen(SyncBudgetNavigator(nav), route.budgetId, route.token)
    }

    composableWithArg<TransactionsNavRoute>(mapOf(BudgetIdType, LoginTokenType)) { route, _ ->
      TransactionsScreen(TransactionsNavigator(nav), route.budgetId, route.token)
    }
  }
}
