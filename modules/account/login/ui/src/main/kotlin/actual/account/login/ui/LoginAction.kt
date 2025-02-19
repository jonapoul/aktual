package actual.account.login.ui

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface LoginAction {
  data object ChangeServer : LoginAction
  data object NavBack : LoginAction
  data object SignIn : LoginAction

  data class EnterPassword(val password: String) : LoginAction
}
