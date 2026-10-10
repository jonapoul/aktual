package aktual.budget.home.vm

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.BankSyncStatus
import aktual.budget.model.CategoryId
import kotlinx.collections.immutable.ImmutableList

internal data class NeedsAttention(
  val failedAccounts: ImmutableList<FailedAccount>,
  val uncategorisedCount: Int,
  val overspent: ImmutableList<OverspentCategory>,
  val overdueSchedules: Int,
)

internal data class FailedAccount(val id: AccountId, val name: String, val status: BankSyncStatus)

// The balance is negative
data class OverspentCategory(val id: CategoryId, val name: String, val balance: Amount)
