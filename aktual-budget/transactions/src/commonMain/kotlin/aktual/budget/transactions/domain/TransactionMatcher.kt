package aktual.budget.transactions.domain

import aktual.budget.model.AccountSpec
import aktual.budget.model.ConditionsFilter
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionSearch

// The transaction lists that are matched in memory, since they can't be expressed as SQL
interface TransactionMatcher {
  // A report's drilled down list: the non-parent transactions matching [filter]
  suspend fun matching(filter: ConditionsFilter): List<TransactionId>

  // A split takes one slot, as its parent, if the parent or any of its children match
  suspend fun search(
    accounts: AccountSpec,
    search: TransactionSearch,
    hideReconciled: Boolean,
  ): SearchMatches
}

// The ids to page over, in list order, with the split children to show under them
data class SearchMatches(
  val ids: List<TransactionId> = emptyList(),
  val children: Set<TransactionId> = emptySet(),
)
