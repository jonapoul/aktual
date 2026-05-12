package aktual.core.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey

@Immutable
class LoginNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(LoginNavRoute)
}

@Immutable
class ServerUrlNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.replaceAll(ServerUrlNavRoute)
}

@Immutable
class ChangePasswordNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(ChangePasswordNavRoute)
}
