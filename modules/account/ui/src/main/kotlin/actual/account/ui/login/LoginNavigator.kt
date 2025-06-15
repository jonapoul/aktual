package actual.account.ui.login

import actual.account.model.LoginToken
import androidx.compose.runtime.Immutable

@Immutable
interface LoginNavigator {
  fun back(): Boolean
  fun toUrl()
  fun toListBudgets(token: LoginToken)
}
