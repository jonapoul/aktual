package aktual.budget.payees.domain

import aktual.budget.model.PayeeId

// What a merge changed, so it can be undone. Each repointed payee_mapping row maps to its old
// target
data class PayeeMerge(
  val target: PayeeId,
  val merged: List<PayeeId>,
  val oldTargets: Map<PayeeId, PayeeId>,
)

interface PayeeMerger {
  // Null when there's nothing to merge. Transfer payees are skipped, as upstream
  suspend fun merge(target: PayeeId, ids: Collection<PayeeId>): PayeeMerge?

  suspend fun undo(merge: PayeeMerge)
}
