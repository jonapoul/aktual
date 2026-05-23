package aktual.account.ui.login

import aktual.core.model.LoginMethod
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface LoginAction

internal data object ChangeServer : LoginAction

internal data object NavBack : LoginAction

internal data object SignIn : LoginAction

@JvmInline internal value class EnterPassword(val password: String) : LoginAction

@JvmInline internal value class SelectLoginMethod(val method: LoginMethod) : LoginAction

@Immutable
internal fun interface LoginActionHandler {
  operator fun invoke(action: LoginAction)
}
