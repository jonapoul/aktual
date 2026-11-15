package aktual.budget.rules.domain

import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.model.PayeeId
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Counts the rules referring to each payee, as getPayeeRuleCounts in
 * packages/loot-core/src/server/payees/app.ts. Rules of completed schedules are left out. Unlike
 * upstream, a rule counts once per payee, however many times it mentions it.
 */
@Inject
class PayeeRuleCounter(private val rulesDao: RulesDao, private val contextDao: RuleContextDao) {
  // Emits whenever the counts might have changed
  fun observeChanges(): Flow<Unit> =
    combine(rulesDao.observeChanges(), contextDao.observeCompletedScheduleRuleChanges()) { _, _ -> }

  suspend fun count(): Map<PayeeId, Int> {
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
