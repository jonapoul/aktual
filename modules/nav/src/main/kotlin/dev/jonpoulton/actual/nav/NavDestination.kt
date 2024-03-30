package dev.jonpoulton.actual.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import dev.jonpoulton.actual.login.ui.LoginScreen
import dev.jonpoulton.actual.serverurl.ui.ServerUrlScreen

sealed class NavDestination(
  val route: String,
  val composable: @Composable (NavHostController) -> Unit,
) {
  data object ServerUrl : NavDestination(
    route = "server-url",
    composable = { ServerUrlScreen(ServerUrlNavigator(it)) },
  )

  data object Login : NavDestination(
    route = "login",
    composable = { LoginScreen(LoginNavigator(it)) },
  )

  data object ListBudgets : NavDestination(
    route = "listBudgets",
    composable = {
      // TODO: implement
    },
  )

  data object Bootstrap : NavDestination(
    route = "bootstrap",
    composable = {
      // TODO: Implement
    },
  )
}
