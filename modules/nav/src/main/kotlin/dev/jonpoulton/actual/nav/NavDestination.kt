package dev.jonpoulton.actual.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import dev.jonpoulton.actual.login.ui.LoginScreen

sealed class NavDestination(
  val route: String,
  val composable: @Composable (NavHostController) -> Unit,
) {
  data object Login : NavDestination(
    route = "login",
    composable = { LoginScreen(it) },
  )
}
