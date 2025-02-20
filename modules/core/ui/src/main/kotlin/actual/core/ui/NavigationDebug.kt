package actual.core.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions

fun <T : Any> NavHostController.debugNavigate(route: T, builder: NavOptionsBuilder.() -> Unit) {
  printBackStack()
  Log.v("debugNavigate", "debugNavigate to $route with builder")
  navigate(route, navOptions(builder))
}

fun <T : Any> NavHostController.debugNavigate(route: T) {
  printBackStack()
  Log.v("debugNavigate", "debugNavigate to $route")
  navigate(route)
}

@SuppressLint("RestrictedApi", "LogNotTimber")
private fun NavHostController.printBackStack() {
  val backstack = currentBackStack.value
  val str = backstack.joinToString { it.destination.toString() }
  Log.v("printBackStack", "backstack = $str")
}
