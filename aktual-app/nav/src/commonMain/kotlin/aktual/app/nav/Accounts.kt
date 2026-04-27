package aktual.app.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
class LoginNavigator(private val stack: AktualNavStack<NavKey>) {
  operator fun invoke() = stack.push(LoginNavRoute)
}

@Immutable @Serializable data object LoginNavRoute : NavKey

@Immutable
class ServerUrlNavigator(private val stack: AktualNavStack<NavKey>) {
  operator fun invoke() = stack.replaceAll(ServerUrlNavRoute)
}

@Immutable @Serializable data object ServerUrlNavRoute : NavKey

@Immutable
class ChangePasswordNavigator(private val stack: AktualNavStack<NavKey>) {
  operator fun invoke() = stack.push(ChangePasswordNavRoute)
}

@Immutable @Serializable data object ChangePasswordNavRoute : NavKey
