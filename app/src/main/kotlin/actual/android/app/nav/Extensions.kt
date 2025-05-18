package actual.android.app.nav

import alakazam.kotlin.logging.Logger
import android.annotation.SuppressLint
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions

internal fun <T : Any> NavHostController.debugNav(route: T, builder: NavOptionsBuilder.() -> Unit) {
  printBackStack()
  Logger.v("debugNavigate", "debugNavigate to %s with builder", route)
  navigate(route, navOptions(builder))
}

internal fun <T : Any> NavHostController.debugNav(route: T) {
  printBackStack()
  Logger.v("debugNavigate", "debugNavigate to %s", route)
  navigate(route)
}

@SuppressLint("RestrictedApi")
fun NavHostController.printBackStack() {
  val backstack = currentBackStack.value
  val str = backstack.joinToString { it.destination.toString() }
  Logger.v("printBackStack", "backstack = $str")
}
