package dev.jonpoulton.actual.nav

import androidx.navigation.NavHostController
import dev.jonpoulton.actual.serverurl.ui.ServerUrlNavigator

internal fun ServerUrlNavigator(navController: NavHostController): ServerUrlNavigator {
  return ServerUrlNavigator {
    navController.navigate(route = NavDestination.Login.route)
  }
}
