package aktual.budget.home.domain

import aktual.budget.db.GetAllWithBalances
import aktual.budget.db.dao.AccountDao
import aktual.budget.model.Amount
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
class AccountsSummaryLoader(private val accountDao: AccountDao) {
  fun observe(): Flow<AccountsSummary> =
    accountDao.observeAllWithBalances().map { rows -> rows.toAccountsSummary() }
}

internal fun List<GetAllWithBalances>.toAccountsSummary(): AccountsSummary {
  val (closed, open) = partition { it.closed == true }
  val (offBudget, onBudget) = open.partition { it.offbudget == true }
  return AccountsSummary(
    onBudget = onBudget.toSection(),
    offBudget = offBudget.toSection(),
    closed = closed.map { it.toAccountBalance() }.toImmutableList(),
  )
}

private fun List<GetAllWithBalances>.toSection(): AccountSection {
  val accounts = map { it.toAccountBalance() }.toImmutableList()
  return AccountSection(accounts, total = accounts.fold(Amount.Zero) { sum, a -> sum + a.balance })
}

private fun GetAllWithBalances.toAccountBalance() =
  AccountBalance(id = id, name = name.orEmpty(), balance = Amount(balance), syncState = syncState())

// Upstream treats an account as linked if it has a sync source, and unlinking clears both
private fun GetAllWithBalances.syncState(): AccountSyncState {
  val status = bank_sync_status
  return when {
    account_sync_source == null || account_id == null -> NotLinked
    status != null && status.isFailure -> AccountSyncState.Failed(status)
    else -> AccountSyncState.Ok(last_sync)
  }
}
