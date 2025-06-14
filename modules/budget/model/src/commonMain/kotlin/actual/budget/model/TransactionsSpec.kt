package actual.budget.model

import androidx.compose.runtime.Immutable

@Immutable
data class TransactionsSpec(
  val accountView: AccountView,
)

@Immutable
sealed interface AccountView {
  data object AllAccounts : AccountView
  data class Account(val id: AccountId) : AccountView
}
