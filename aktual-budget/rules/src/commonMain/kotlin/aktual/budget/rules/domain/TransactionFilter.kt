package aktual.budget.rules.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.PayeeId
import kotlinx.datetime.LocalDate

/** A transaction as a report filter sees it, with the names that id conditions can match */
data class FilterRow(
  val date: LocalDate,
  val amount: Amount,
  val account: AccountId,
  val accountName: String? = null,
  val offBudget: Boolean = false,
  val payee: PayeeId? = null,
  val payeeName: String? = null,
  // The other side of a transfer, whose payee has no name of its own
  val transferAccountName: String? = null,
  val importedPayee: String? = null,
  val category: CategoryId? = null,
  val categoryName: String? = null,
  val categoryGroup: CategoryGroupId? = null,
  val categoryGroupName: String? = null,
  val notes: String? = null,
  val cleared: Boolean = false,
  val reconciled: Boolean = false,
  val isTransfer: Boolean = false,
  val isParent: Boolean = false,
)

/**
 * Ids and dates that every transaction a [TransactionFilter] matches is within. A null set or date
 * doesn't narrow anything
 */
data class FilterBounds(
  val from: LocalDate? = null,
  val to: LocalDate? = null,
  val accounts: Set<String>? = null,
  val categories: Set<String>? = null,
  val payees: Set<String>? = null,
)

// Matches transactions against a report's filter conditions
interface TransactionFilter {
  // True when there's nothing to filter by, so every transaction matches
  val isEmpty: Boolean

  /**
   * What every matching transaction has, for narrowing a query before [matches] runs. Only all
   * conditions together narrow it, since any one of them could match on its own
   */
  val bounds: FilterBounds

  fun matches(row: FilterRow): Boolean

  /**
   * Whether a transaction on [date] could match, judging only by date conditions. Other conditions
   * could match on any date, unless they match nothing at all
   */
  fun couldMatchOn(date: LocalDate): Boolean
}

fun interface TransactionFilterCompiler {
  // Invalid conditions and references to saved filters are skipped
  fun compile(conditions: List<Condition>, op: ConditionOp): TransactionFilter
}
