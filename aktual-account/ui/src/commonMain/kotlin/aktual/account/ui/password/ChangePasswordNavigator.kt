package aktual.account.ui.password

import aktual.core.model.Token
import androidx.compose.runtime.Immutable

@Immutable
interface ChangePasswordNavigator {
  fun back(): Boolean

  fun toListBudgets(token: Token)
}
