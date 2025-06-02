package actual.android.app.nav

import actual.about.info.ui.InfoScreen
import actual.about.licenses.ui.LicensesScreen
import actual.account.login.ui.LoginScreen
import actual.account.model.LoginToken
import actual.account.password.ui.ChangePasswordScreen
import actual.budget.list.ui.ListBudgetsScreen
import actual.budget.sync.ui.SyncBudgetScreen
import actual.settings.ui.SettingsScreen
import actual.url.ui.ServerUrlScreen
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

    composable<ServerUrlNavRoute> { ServerUrlScreen(ServerUrlNavigator(nav)) }

    composable<SettingsNavRoute> { SettingsScreen(SettingsNavigator(nav)) }

    composableWithArg<SyncBudgetsNavRoute>(mapOf(BudgetIdType, LoginTokenType)) { route, _ ->
      SyncBudgetScreen(SyncBudgetNavigator(nav), route.budgetId, route.token)
    }
  }
}
