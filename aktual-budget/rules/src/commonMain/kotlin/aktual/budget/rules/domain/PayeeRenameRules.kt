package aktual.budget.rules.domain

import aktual.budget.model.PayeeId

/**
 * updatePayeeRenameRule() in packages/loot-core/src/server/transactions/transaction-rules.ts. Adds
 * the names to the first pre-stage `imported_payee oneOf [..] -> set payee` rule for the payee, or
 * adds one, so imported transactions with those names get renamed.
 */
fun interface PayeeRenameRules {
  suspend fun add(fromNames: Collection<String>, to: PayeeId)
}
