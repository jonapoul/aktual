package aktual.budget.rules.domain

import aktual.budget.model.PayeeId

/**
 * Loads the open budget's rules, and what they need to know about it, from its database. Only
 * available in the `BudgetScope` graph, since it reads the budget database.
 */
interface TransactionRulesLoader {
  suspend fun load(): LoadedRules
}

/**
 * A snapshot of a budget's rules, ready to run against transactions. Payees that rules rename
 * transactions to but don't exist yet are collected in [createdPayees], and the caller needs to
 * create them when it saves the transactions.
 */
interface LoadedRules {
  val createdPayees: Map<PayeeId, String>

  fun run(transaction: RuleTransaction): RuleTransaction

  fun resolvePayee(name: String): PayeeId
}
