package aktual.budget.banksync.domain

import aktual.budget.model.AccountId
import aktual.di.BudgetScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * How many transactions bank sync has added to each account since it was last opened, like
 * updatedAccounts in packages/desktop-client/src/accounts/accountsSlice.ts. Only kept in memory, so
 * it starts empty each time the budget opens.
 */
@Inject
@SingleIn(BudgetScope::class)
class NewTransactions {
  private val mutableCounts = MutableStateFlow<PersistentMap<AccountId, Int>>(persistentMapOf())
  val counts: StateFlow<PersistentMap<AccountId, Int>> = mutableCounts.asStateFlow()

  operator fun plusAssign(results: List<BankSyncResult>) {
    mutableCounts.update { counts ->
      results
        .filterIsInstance<BankSyncResult.Synced>()
        .filter { it.added.isNotEmpty() }
        .fold(counts) { acc, result ->
          acc.putting(result.account, acc.getOrElse(result.account) { 0 } + result.added.size)
        }
    }
  }

  fun markRead(account: AccountId) {
    mutableCounts.update { it.removing(account) }
  }
}
