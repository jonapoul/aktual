package aktual.account.ui.password

import aktual.core.model.LoginToken
import androidx.compose.runtime.Immutable

@Immutable
interface ChangePasswordNavigator {
  fun back(): Boolean
  fun toListBudgets(token: LoginToken)
}
