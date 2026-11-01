package aktual.budget.home.vm

import aktual.budget.banksync.domain.NewTransactions
import aktual.budget.db.GetAllWithBalances
import aktual.budget.db.dao.AccountDao
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncState
import aktual.budget.model.Amount
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Inject
internal class AccountsSummaryLoader(
  private val accountDao: AccountDao,
  private val newTransactions: NewTransactions,
) {
  fun observe(): Flow<AccountsSummary> =
    combine(accountDao.observeAllWithBalances(), newTransactions.counts) { rows, counts ->
      rows.toAccountsSummary(counts)
    }
}

internal fun List<GetAllWithBalances>.toAccountsSummary(
  newTransactions: Map<AccountId, Int> = emptyMap(),
): AccountsSummary {
  val (closed, open) = partition { it.closed == true }
  val (offBudget, onBudget) = open.partition { it.offbudget == true }
  return AccountsSummary(
    onBudget = onBudget.toSection(newTransactions),
    offBudget = offBudget.toSection(newTransactions),
    closed = closed.map { it.toAccountBalance(newTransactions) }.toImmutableList(),
  )
}

private fun List<GetAllWithBalances>.toSection(
  newTransactions: Map<AccountId, Int>,
): AccountSection {
  val accounts = map { it.toAccountBalance(newTransactions) }.toImmutableList()
  return AccountSection(accounts, total = accounts.fold(Zero) { sum, a -> sum + a.balance })
}

private fun GetAllWithBalances.toAccountBalance(newTransactions: Map<AccountId, Int>) =
  AccountBalance(
    id = id,
    name = name.orEmpty(),
    balance = Amount(balance),
    syncState = AccountSyncState(account_sync_source, account_id, bank_sync_status, last_sync),
    lastActivity = last_activity,
    newTransactions = newTransactions[id] ?: 0,
  )
