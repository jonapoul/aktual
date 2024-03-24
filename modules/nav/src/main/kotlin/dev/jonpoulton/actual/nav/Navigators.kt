package dev.jonpoulton.actual.nav

import androidx.navigation.NavHostController
import dev.jonpoulton.actual.login.ui.LoginNavigator
import dev.jonpoulton.actual.serverurl.ui.ServerUrlNavigator

internal fun ServerUrlNavigator(navController: NavHostController): ServerUrlNavigator {
  return object : ServerUrlNavigator {
    override fun navigateToLogin() {
      navController.navigate(route = NavDestination.Login.route)
    }

    override fun navigateToBootstrap() {
      navController.navigate(route = NavDestination.Bootstrap.route)
    }
  }
}

internal fun LoginNavigator(navController: NavHostController): LoginNavigator {
  return object : LoginNavigator {
    override fun changeServer() {
      navController.popBackStack()
    }

    override fun syncBudget() {
      navController.navigate(route = NavDestination.SyncBudget.route)
    }
  }
}
