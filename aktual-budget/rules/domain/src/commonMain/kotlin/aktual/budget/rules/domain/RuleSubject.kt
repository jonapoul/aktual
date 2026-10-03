package aktual.budget.rules.domain

import aktual.budget.model.CategoryGroupId

/**
 * The transaction while rules run, plus the values upstream's `prepareTransactionForRules` adds for
 * conditions to read: `payee_name`, `category_group` and `_account`.
 */
internal data class RuleSubject(
  val transaction: RuleTransaction,
  val payeeName: String? = null,
  // Null when the transaction has no category (upstream leaves the field undefined, so no
  // category_group condition matches)
  val categoryGroup: CategoryGroupId? = null,
  val account: RuleAccount? = null,
  // A "set payee_name" action ran, so the payee should be looked up by [payeeName]. Upstream marks
  // this by setting the payee to the string "new"
  val newPayeePending: Boolean = false,
)
