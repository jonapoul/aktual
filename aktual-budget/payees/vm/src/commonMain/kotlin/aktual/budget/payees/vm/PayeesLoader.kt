package aktual.budget.payees.vm

import aktual.budget.db.GetPayeeList
import aktual.budget.db.dao.PayeeDao
import aktual.budget.model.PayeeId
import aktual.budget.payees.vm.list.PayeeItem
import aktual.budget.rules.domain.PayeeRuleCounter
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest

@Inject
internal class PayeesLoader(
  private val payeeDao: PayeeDao,
  private val ruleCounter: PayeeRuleCounter,
) {
  // Reloads whenever the payees, their transactions or the rules change, so synced and local edits
  // show up without a manual refresh
  fun observe(): Flow<List<PayeeItem>> =
    combine(payeeDao.observeListChanges(), ruleCounter.observeChanges()) { _, _ -> }
      .mapLatest { load() }

  private suspend fun load(): List<PayeeItem> {
    val ruleCounts = ruleCounter.count()
    return payeeDao.list().map { it.toPayeeItem(ruleCounts) }
  }

  private fun GetPayeeList.toPayeeItem(ruleCounts: Map<PayeeId, Int>) =
    PayeeItem(
      id = id,
      name = name.orEmpty(),
      isTransfer = transfer_acct != null,
      isFavorite = favorite == true,
      numTransactions = num_transactions.toInt(),
      numRules = ruleCounts[id] ?: 0,
    )
}
