package actual.account.password.ui

import actual.account.model.Password
import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko

@Immutable
internal sealed interface PasswordAction {
  data object NavBack : PasswordAction
  data object Submit : PasswordAction
  @Poko class SetPasswordsVisible(val visible: Boolean) : PasswordAction
  @Poko class SetPassword1(val value: Password) : PasswordAction
  @Poko class SetPassword2(val value: Password) : PasswordAction
}
