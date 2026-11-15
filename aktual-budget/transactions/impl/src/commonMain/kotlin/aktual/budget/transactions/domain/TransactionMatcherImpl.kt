package aktual.budget.transactions.domain

import aktual.budget.db.dao.RowBounds
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSpec
import aktual.budget.model.CategoryId
import aktual.budget.model.ConditionsFilter
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionSearch
import aktual.budget.rules.domain.ConditionsMatcher
import aktual.budget.rules.domain.FilterBounds
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(BudgetScope::class)
class TransactionMatcherImpl(private val transactionDao: TransactionDao) : TransactionMatcher {
  override suspend fun matching(filter: ConditionsFilter): List<TransactionId> {
    val matcher = ConditionsMatcher(filter)
    return transactionDao
      .getFilterRows(skipDeleted = false)
      .filter { it.is_parent == false && matcher.matches(it.toFilterRow()) }
      .map { it.id }
  }

  override suspend fun search(
    accounts: AccountSpec,
    search: TransactionSearch,
    hideReconciled: Boolean,
  ): SearchMatches {
    val matcher = TransactionSearchMatcher(search)
    val text = matcher.text
    val cents = matcher.amount
    val bounds = matcher.bounds.toRowBounds()
    val rows =
      when (accounts) {
        AllAccounts -> transactionDao.getFilterRows(text = text, cents = cents, bounds = bounds)
        is SpecificAccount ->
          transactionDao.getFilterRows(
            account = accounts.id,
            text = text,
            cents = cents,
            bounds = bounds,
          )
        is Group ->
          transactionDao.getFilterRows(
            group = accounts.group,
            text = text,
            cents = cents,
            bounds = bounds,
          )
      }
    val matching = rows.filter { row ->
      !(hideReconciled && row.reconciled == true) && matcher.matches(row.toFilterRow())
    }
    val (children, topLevel) = matching.partition { it.is_child == true }
    val ids = topLevel.mapTo(mutableSetOf()) { it.id }
    children.mapNotNullTo(ids) { it.parent_id }
    return SearchMatches(
      ids = rows.filter { it.is_child != true && it.id in ids }.map { it.id },
      children = children.mapTo(mutableSetOf()) { it.id },
    )
  }

  private fun FilterBounds.toRowBounds() =
    RowBounds(
      from = from,
      to = to,
      accounts = accounts?.mapTo(mutableSetOf(), ::AccountId),
      categories = categories?.mapTo(mutableSetOf(), ::CategoryId),
      payees = payees?.mapTo(mutableSetOf(), ::PayeeId),
    )
}
