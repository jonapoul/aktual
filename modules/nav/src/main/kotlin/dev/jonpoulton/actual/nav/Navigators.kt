package dev.jonpoulton.actual.nav

import android.annotation.SuppressLint
import androidx.navigation.NavHostController
import dev.jonpoulton.actual.login.ui.LoginNavigator
import dev.jonpoulton.actual.serverurl.ui.ServerUrlNavigator
import timber.log.Timber

internal fun ServerUrlNavigator(navController: NavHostController): ServerUrlNavigator {
  return object : ServerUrlNavigator {
    override fun navigateToLogin() {
      navController.printBackStack()
      navController.navigate(route = NavDestination.Login.route)
    }

    override fun navigateToBootstrap() {
      navController.printBackStack()
      navController.navigate(route = NavDestination.Bootstrap.route)
    }
  }
}

internal fun LoginNavigator(navController: NavHostController): LoginNavigator {
  return object : LoginNavigator {
    override fun changeServer() {
      navController.printBackStack()
      navController.popBackStack()
    }

    override fun syncBudget() {
      navController.printBackStack()
      navController.navigate(route = NavDestination.SyncBudget.route)
    }
  }
}

@SuppressLint("RestrictedApi")
private fun NavHostController.printBackStack() {
  val backstack = currentBackStack.value
  val str = backstack.joinToString { it.destination.toString() }
  Timber.v("backstack = $str")
}
