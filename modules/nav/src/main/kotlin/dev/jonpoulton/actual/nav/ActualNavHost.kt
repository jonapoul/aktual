package dev.jonpoulton.actual.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun ActualNavHost(
  isServerUrlSet: Boolean,
  navController: NavHostController = rememberNavController(),
) {
  NavHost(
    navController = navController,
    startDestination = when {
      isServerUrlSet -> NavDestination.Login.route
      else -> NavDestination.ServerUrl.route
    },
  ) {
    composable(navController, NavDestination.ServerUrl)
    composable(navController, NavDestination.Login)
    composable(navController, NavDestination.Bootstrap)
    composable(navController, NavDestination.SyncBudget)
  }
}
