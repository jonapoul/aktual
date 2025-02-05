package actual.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

internal fun NavGraphBuilder.composable(navController: NavHostController, destination: NavDestination) {
  composable(route = destination.route) {
    destination.composable(navController)
  }
}
