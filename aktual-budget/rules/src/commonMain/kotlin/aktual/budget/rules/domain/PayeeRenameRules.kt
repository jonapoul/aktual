package aktual.budget.rules.domain

import aktual.budget.model.Condition
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId

/**
 * updatePayeeRenameRule() in packages/loot-core/src/server/transactions/transaction-rules.ts. Adds
 * the names to the first pre-stage `imported_payee oneOf [..] -> set payee` rule for the payee, or
 * adds one, so imported transactions with those names get renamed.
 */
interface PayeeRenameRules {
  // Null when nothing changed
  suspend fun add(fromNames: Collection<String>, to: PayeeId): PayeeRename?

  suspend fun undo(rename: PayeeRename)
}

// What add() changed, so it can be undone
sealed interface PayeeRename {
  @JvmInline value class Created(val rule: RuleId) : PayeeRename

  data class Extended(val rule: RuleId, val oldCondition: Condition) : PayeeRename
}
