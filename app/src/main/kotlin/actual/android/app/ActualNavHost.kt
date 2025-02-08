package actual.android.app

import actual.budget.list.nav.ListBudgetsNavRoute
import actual.budget.list.ui.ListBudgetsScreen
import actual.licenses.nav.LicensesNavRoute
import actual.licenses.ui.LicensesScreen
import actual.login.nav.LoginNavRoute
import actual.login.ui.LoginScreen
import actual.url.nav.ServerUrlNavRoute
import actual.url.ui.ServerUrlScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
internal fun ActualNavHost(
  isServerUrlSet: Boolean,
  navController: NavHostController = rememberNavController(),
) {
  NavHost(
    navController = navController,
    startDestination = when {
      isServerUrlSet -> LoginNavRoute
      else -> ServerUrlNavRoute
    },
  ) {
    composable<LicensesNavRoute> { LicensesScreen(navController) }
    composable<ListBudgetsNavRoute> { ListBudgetsScreen(navController) }
    composable<LoginNavRoute> { LoginScreen(navController) }
    composable<ServerUrlNavRoute> { ServerUrlScreen(navController) }
  }
}
