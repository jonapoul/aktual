package aktual.budget.transactions.domain

import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.budget.rules.domain.RuleTransaction
import aktual.budget.rules.domain.TransactionRule

/**
 * The transactions a draft rule's conditions match, and what its actions would do to them, as the
 * rule editor in upstream's RuleEditor.tsx shows them. Conditions match the way a transaction
 * filter does, so with `make-filters-from-conditions` rather than how rules run. Split parents
 * never match, and nor do split children if the rule makes splits.
 */
interface RuleMatcher {
  /** Null if the rule's actions are invalid */
  suspend fun find(rule: TransactionRule): RuleMatches?

  /**
   * Runs the rule's actions on the [selected] transactions it matches, as `rule-apply-actions`
   * does, and returns how many it changed
   */
  suspend fun apply(rule: TransactionRule, selected: Set<TransactionId>): Int
}

data class RuleMatches(
  // Newest first
  val matches: List<RuleMatch>,
  // The names of the payees, categories and accounts before and after, by id
  val names: Map<String, String>,
  // Payees the actions would create, which applying them inserts
  val createdPayees: Map<PayeeId, String> = emptyMap(),
)

data class RuleMatch(val before: RuleTransaction, val after: RuleTransaction) {
  val isChanged: Boolean
    get() = before != after
}
