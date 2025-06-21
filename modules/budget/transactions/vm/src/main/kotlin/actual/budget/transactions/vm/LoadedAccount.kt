package actual.budget.transactions.vm

import actual.budget.db.Accounts
import androidx.compose.runtime.Immutable

@Immutable
sealed interface LoadedAccount {
  data object Loading : LoadedAccount
  data object AllAccounts : LoadedAccount
  data class SpecificAccount(val account: Accounts) : LoadedAccount
}
