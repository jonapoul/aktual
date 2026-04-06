package aktual.app.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey

@Immutable
fun interface BackNavigator {
  operator fun invoke()
}

fun <T : NavKey> BackNavigator(stack: AktualNavStack<T>): BackNavigator = BackNavigator {
  stack.pop()
}
