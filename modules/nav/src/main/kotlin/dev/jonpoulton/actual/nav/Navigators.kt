package dev.jonpoulton.actual.nav

import android.annotation.SuppressLint
import androidx.navigation.NavHostController
import dev.jonpoulton.actual.listbudgets.ui.ListBudgetsNavigator
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
    override fun navBack() {
      navController.printBackStack()
      navController.popBackStack()
    }

    override fun changeServer() {
      navController.printBackStack()
      navController.navigate(NavDestination.ServerUrl.route) {
        popUpTo(NavDestination.Login.route) {
          inclusive = true
        }
      }
    }

    override fun listBudgets() {
      navController.printBackStack()
      navController.navigate(NavDestination.ListBudgets.route) {
        popUpTo(NavDestination.Login.route) {
          inclusive = true
        }
      }
    }
  }
}

internal fun ListBudgetsNavigator(navController: NavHostController): ListBudgetsNavigator {
  return object : ListBudgetsNavigator {
    override fun changeServer() {
      navController.printBackStack()
      navController.navigate(NavDestination.ServerUrl.route) {
        popUpTo(NavDestination.ListBudgets.route) { inclusive = true }
      }
    }

    override fun logOut() {
      navController.printBackStack()
      navController.navigate(NavDestination.Login.route) {
        popUpTo(NavDestination.ListBudgets.route) {
          inclusive = true
        }
      }
    }

    override fun openBudget() {
      navController.printBackStack()
      // TODO: IMPLEMENT
    }
  }
}

@SuppressLint("RestrictedApi")
private fun NavHostController.printBackStack() {
  val backstack = currentBackStack.value
  val str = backstack.joinToString { it.destination.toString() }
  Timber.v("backstack = $str")
}
