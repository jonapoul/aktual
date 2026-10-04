package aktual.budget.home.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.BankSyncStatus
import kotlin.time.Instant
import kotlinx.collections.immutable.ImmutableList

data class AccountsSummary(
  val onBudget: AccountSection,
  val offBudget: AccountSection,
  val closed: ImmutableList<AccountBalance>,
) {
  // Closed accounts don't count, as upstream
  val netWorth: Amount
    get() = onBudget.total + offBudget.total
}

data class AccountSection(val accounts: ImmutableList<AccountBalance>, val total: Amount)

data class AccountBalance(
  val id: AccountId,
  val name: String,
  val balance: Amount,
  val syncState: AccountSyncState,
)

sealed interface AccountSyncState {
  data object NotLinked : AccountSyncState

  data class Ok(val lastSync: Instant?) : AccountSyncState

  data class Failed(val status: BankSyncStatus) : AccountSyncState
}
