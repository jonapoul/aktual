package actual.core.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions

fun <T : Any> NavHostController.debugNavigate(route: T, builder: NavOptionsBuilder.() -> Unit) {
  printBackStack()
  navigate(route, navOptions(builder))
}

fun <T : Any> NavHostController.debugNavigate(route: T) {
  printBackStack()
  navigate(route)
}

@SuppressLint("RestrictedApi", "LogNotTimber")
private fun NavHostController.printBackStack() {
  val backstack = currentBackStack.value
  val str = backstack.joinToString { it.destination.toString() }
  Log.v("printBackStack", "backstack = $str")
}
