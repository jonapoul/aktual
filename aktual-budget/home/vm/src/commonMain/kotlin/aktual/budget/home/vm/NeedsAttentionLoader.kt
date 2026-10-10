package aktual.budget.home.vm

import aktual.budget.budgeting.domain.BudgetMonth
import aktual.budget.db.dao.TransactionDao
import aktual.budget.schedules.domain.SchedulesLoader
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

@Inject
class NeedsAttentionLoader(
  private val accountsSummaryLoader: AccountsSummaryLoader,
  private val transactionDao: TransactionDao,
  private val thisMonthLoader: ThisMonthLoader,
  private val schedulesLoader: SchedulesLoader,
) {
  fun observe(): Flow<NeedsAttention> =
    combine(
        accountsSummaryLoader.observe(),
        transactionDao.observeUncategorisedCount(),
        thisMonthLoader.observe(),
        schedulesLoader.observe(),
      ) { accounts, uncategorised, thisMonth, schedules ->
        NeedsAttention(
          failedAccounts = accounts.failedAccounts(),
          uncategorisedCount = uncategorised.toInt(),
          overspent = thisMonth.budget.overspentCategories(),
          overdueSchedules = schedules.count { it.status == Missed },
        )
      }
      .distinctUntilChanged()
}

// Closed accounts are left out, as they don't sync
internal fun AccountsSummary.failedAccounts(): ImmutableList<FailedAccount> =
  (onBudget.accounts + offBudget.accounts)
    .mapNotNull { account ->
      val state = account.syncState as? AccountSyncState.Failed
      state?.let { FailedAccount(account.id, account.name, it.status) }
    }
    .toImmutableList()

// As upstream's useOverspentCategories: overspending that rolls over isn't flagged. Tracking
// budgets have nowhere to cover it from, so they're skipped.
internal fun BudgetMonth.overspentCategories(): ImmutableList<OverspentCategory> =
  when (this) {
    is Tracking -> persistentListOf()
    is Envelope ->
      categories
        .filter { !it.isIncome && !it.carryover && it.balance < Zero }
        .map { OverspentCategory(it.id, it.name, it.balance) }
        .toImmutableList()
  }
