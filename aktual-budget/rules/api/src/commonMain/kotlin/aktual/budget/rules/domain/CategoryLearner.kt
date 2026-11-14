package aktual.budget.rules.domain

import aktual.budget.model.TransactionId

/**
 * updateCategoryRules() in packages/loot-core/src/server/transactions/transaction-rules.ts. When
 * one category wins among a payee's latest transactions, the payee's simple `payee is X -> set
 * category` rules are pointed at it, or one is added. Does nothing while the learn-categories pref
 * is off.
 */
fun interface CategoryLearner {
  // [ids] are the transactions that were just added or given a category
  suspend fun learn(ids: Collection<TransactionId>)
}
