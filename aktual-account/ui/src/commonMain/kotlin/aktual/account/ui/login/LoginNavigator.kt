package aktual.account.ui.login

import aktual.core.model.LoginToken
import androidx.compose.runtime.Immutable

@Immutable
interface LoginNavigator {
  fun back(): Boolean
  fun toUrl()
  fun toListBudgets(token: LoginToken)
}
