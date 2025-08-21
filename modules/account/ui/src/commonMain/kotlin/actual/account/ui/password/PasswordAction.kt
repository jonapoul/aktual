package actual.account.ui.password

import actual.core.model.Password
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface PasswordAction {
  data object NavBack : PasswordAction
  data object Submit : PasswordAction
  data class SetPasswordsVisible(val visible: Boolean) : PasswordAction
  data class SetPassword1(val value: Password) : PasswordAction
  data class SetPassword2(val value: Password) : PasswordAction
}
