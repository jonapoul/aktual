package aktual.budget.rules.domain

import aktual.budget.model.PayeeId
import kotlinx.coroutines.flow.Flow

/**
 * Counts the rules referring to each payee, as getPayeeRuleCounts in
 * packages/loot-core/src/server/payees/app.ts. Rules of completed schedules are left out. Unlike
 * upstream, a rule counts once per payee, however many times it mentions it.
 */
interface PayeeRuleCounter {
  // Emits whenever the counts might have changed
  fun observeChanges(): Flow<Unit>

  suspend fun count(): Map<PayeeId, Int>
}
