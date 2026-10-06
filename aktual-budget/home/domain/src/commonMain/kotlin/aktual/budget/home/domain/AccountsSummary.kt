package aktual.budget.home.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.BankSyncStatus
import kotlin.time.Instant
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate

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
  val lastActivity: LocalDate?,
)

// Cuts the open accounts down to those with the latest transactions, keeping their order and the
// section totals. Null if that wouldn't hide anything
fun AccountsSummary.mostRecentlyActive(limit: Int): AccountsSummary? {
  val open = onBudget.accounts + offBudget.accounts
  if (open.size <= limit) return null
  val recent = open.sortedByDescending { it.lastActivity }.take(limit).mapTo(HashSet()) { it.id }
  return copy(onBudget = onBudget.only(recent), offBudget = offBudget.only(recent))
}

private fun AccountSection.only(ids: Set<AccountId>) =
  copy(accounts = accounts.filter { it.id in ids }.toImmutableList())

sealed interface AccountSyncState {
  data object NotLinked : AccountSyncState

  data class Ok(val lastSync: Instant?) : AccountSyncState

  data class Failed(val status: BankSyncStatus) : AccountSyncState
}
