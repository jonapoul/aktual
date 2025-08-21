package actual.account.ui.password

import actual.core.model.LoginToken
import androidx.compose.runtime.Immutable

@Immutable
interface ChangePasswordNavigator {
  fun back(): Boolean
  fun toListBudgets(token: LoginToken)
}
