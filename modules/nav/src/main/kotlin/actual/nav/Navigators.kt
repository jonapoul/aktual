@file:Suppress("ktlint:standard:function-naming")

package actual.nav

import actual.budget.list.ui.ListBudgetsNavigator
import actual.login.ui.LoginNavigator
import actual.serverurl.ui.ServerUrlNavigator
import android.annotation.SuppressLint
import androidx.navigation.NavHostController
import timber.log.Timber

internal fun ServerUrlNavigator(navController: NavHostController) = object : ServerUrlNavigator {
  override fun navigateToLogin() {
    navController.printBackStack()
    navController.navigate(route = NavDestination.Login.route)
  }

  override fun navigateToBootstrap() {
    navController.printBackStack()
    navController.navigate(route = NavDestination.Bootstrap.route)
  }
}

internal fun LoginNavigator(navController: NavHostController) = object : LoginNavigator {
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

internal fun ListBudgetsNavigator(navController: NavHostController) = object : ListBudgetsNavigator {
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

@SuppressLint("RestrictedApi")
private fun NavHostController.printBackStack() {
  val backstack = currentBackStack.value
  val str = backstack.joinToString { it.destination.toString() }
  Timber.v("backstack = $str")
}
