package aktual.account.ui.password

import aktual.core.model.Password
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface PasswordAction

internal data object NavBack : PasswordAction

internal data object Submit : PasswordAction

@JvmInline internal value class SetPasswordsVisible(val visible: Boolean) : PasswordAction

@JvmInline internal value class SetPassword1(val value: Password) : PasswordAction

@JvmInline internal value class SetPassword2(val value: Password) : PasswordAction

@Immutable
internal fun interface PasswordActionHandler {
  operator fun invoke(action: PasswordAction)
}
