package aktual.budget.payees.domain

import aktual.budget.db.dao.DatabaseTables.PAYEES
import aktual.budget.db.dao.DatabaseTables.PAYEE_MAPPING
import aktual.budget.db.dao.PayeeDao
import aktual.budget.model.PayeeId
import aktual.budget.model.localChange
import aktual.budget.model.tombstone
import aktual.budget.model.untombstone
import aktual.budget.utils.BudgetSync
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

// Ports db/index.ts mergePayees(). Transactions resolve their payee through payee_mapping, so
// repointing the mappings moves them to the target without rewriting any transaction
@Inject
@ContributesBinding(BudgetScope::class)
class PayeeMergerImpl(
  private val payeeDao: PayeeDao,
  private val budgetSync: BudgetSync,
) : PayeeMerger {
  override suspend fun merge(target: PayeeId, ids: Collection<PayeeId>): PayeeMerge? {
    if (!payeeDao.isMergeable(target)) return null
    val merged = ids.filter { it != target && payeeDao.isMergeable(it) }.distinct()
    if (merged.isEmpty()) return null

    val oldTargets = linkedMapOf<PayeeId, PayeeId>()
    for (id in merged) {
      payeeDao.mappedTo(id).forEach { oldTargets[it] = id }
      if (id !in oldTargets) oldTargets[id] = payeeDao.mappingTarget(id) ?: id
    }

    budgetSync(
      oldTargets.keys.map { localChange(PAYEE_MAPPING, it.value, "targetId", target.value) } +
        merged.map { tombstone(PAYEES, it.value) },
    )
    return PayeeMerge(target, merged, oldTargets)
  }

  override suspend fun undo(merge: PayeeMerge) {
    budgetSync(
      merge.merged.map { untombstone(PAYEES, it.value) } +
        merge.oldTargets.map { (id, old) ->
          localChange(PAYEE_MAPPING, id.value, "targetId", old.value)
        },
    )
  }

  private suspend fun PayeeDao.isMergeable(id: PayeeId): Boolean {
    val payee = get(id) ?: return false
    return payee.tombstone != true && payee.transfer_acct == null
  }
}
