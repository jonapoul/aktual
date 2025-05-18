package actual.account.password.ui

import actual.account.model.LoginToken
import androidx.compose.runtime.Immutable

@Immutable
interface ChangePasswordNavigator {
  fun back(): Boolean
  fun toListBudgets(token: LoginToken)
}
