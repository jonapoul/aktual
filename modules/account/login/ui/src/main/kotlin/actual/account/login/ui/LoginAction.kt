package actual.account.login.ui

import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko

@Immutable
internal sealed interface LoginAction {
  data object ChangeServer : LoginAction
  data object NavBack : LoginAction
  data object SignIn : LoginAction

  @Poko class EnterPassword(val password: String) : LoginAction
}
