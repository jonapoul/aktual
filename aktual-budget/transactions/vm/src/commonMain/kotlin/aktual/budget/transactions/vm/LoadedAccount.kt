package aktual.budget.transactions.vm

import aktual.budget.db.Accounts
import androidx.compose.runtime.Immutable

@Immutable
sealed interface LoadedAccount {
  data object Loading : LoadedAccount

  data object AllAccounts : LoadedAccount

  data class SpecificAccount(val account: Accounts) : LoadedAccount
}
