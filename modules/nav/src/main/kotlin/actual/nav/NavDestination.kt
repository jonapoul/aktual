package actual.nav

import actual.budget.list.ui.ListBudgetsScreen
import actual.login.ui.LoginScreen
import actual.url.ui.ServerUrlScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

sealed class NavDestination(
  val route: String,
  val composable: @Composable (NavHostController) -> Unit,
) {
  data object ServerUrl : NavDestination(
    route = "server-url",
    composable = { controller -> ServerUrlScreen(ServerUrlNavigator(controller)) },
  )

  data object Login : NavDestination(
    route = "login",
    composable = { controller -> LoginScreen(LoginNavigator(controller)) },
  )

  data object ListBudgets : NavDestination(
    route = "listBudgets",
    composable = { controller -> ListBudgetsScreen(ListBudgetsNavigator(controller)) },
  )

  data object Bootstrap : NavDestination(
    route = "bootstrap",
    composable = {
      // TODO: Implement
    },
  )
}
