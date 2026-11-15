package aktual.budget.rules.domain

import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.model.PayeeId
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Inject
@ContributesBinding(BudgetScope::class)
class PayeeRuleCounterImpl(private val rulesDao: RulesDao, private val contextDao: RuleContextDao) :
  PayeeRuleCounter {
  override fun observeChanges(): Flow<Unit> =
    combine(rulesDao.observeChanges(), contextDao.observeCompletedScheduleRuleChanges()) { _, _ -> }

  override suspend fun count(): Map<PayeeId, Int> {
    val completed = contextDao.completedScheduleRules()
    val counts = HashMap<PayeeId, Int>()
    for (row in rulesDao.getAll()) {
      if (row.id in completed) continue
      for (id in row.toTransactionRule().payeeIds()) {
        counts[id] = (counts[id] ?: 0) + 1
      }
    }
    return counts
  }
}
