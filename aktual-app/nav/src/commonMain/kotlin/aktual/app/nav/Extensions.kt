package aktual.app.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import dev.jonpoulton.preferences.core.Preference
import logcat.logcat

internal fun <T : Any> NavHostController.debugNav(route: T, builder: NavOptionsBuilder.() -> Unit) {
  logcat.v(TAG) { "Nav to $route with builder - backStack=[$backStack]" }
  navigate(route, navOptions(builder))
}

internal fun <T : Any> NavHostController.debugNav(route: T) {
  logcat.v(TAG) { "Nav to $route - backStack=[$backStack]" }
  navigate(route)
}

private val NavHostController.backStack
  get() =
    currentBackStack.value.joinToString { entry ->
      "route=${entry.destination.route},args=${entry.arguments}"
    }

private const val TAG = "debugNavigate"

@Composable
internal fun <T> Preference<T>.collectAsStateWithDefault(): State<T> =
  asFlow().collectAsState(default)
