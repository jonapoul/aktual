package aktual.budget.rules.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.ScheduleId
import aktual.budget.model.TransactionId
import aktual.budget.model.tagsInNotes
import kotlinx.datetime.LocalDate

/**
 * The transaction that rules read and change. Mirrors the fields of upstream's
 * `TransactionForRules` (packages/loot-core/src/server/transactions/transaction-rules.ts) that
 * rules can see, using the public field names rather than the `transactions` table's columns.
 *
 * Children created by a split action have a null [id] and [isChild] set; the writer gives them ids
 * when it saves the split.
 */
data class RuleTransaction(
  val account: AccountId,
  val date: LocalDate,
  val amount: Amount,
  val id: TransactionId? = null,
  val payee: PayeeId? = null,
  val importedPayee: String? = null,
  val category: CategoryId? = null,
  val notes: String? = null,
  val cleared: Boolean = true,
  val reconciled: Boolean = false,
  val schedule: ScheduleId? = null,
  val isParent: Boolean = false,
  val isChild: Boolean = false,
  // Set by a delete-transaction action
  val tombstone: Boolean = false,
  val subtransactions: List<RuleTransaction> = emptyList(),
) {
  // The distinct tag names (lowercased, without the leading '#') in the notes
  val tags: Set<String>
    get() = notes?.let(::tagsInNotes).orEmpty()

  /**
   * How far the children of a split are from adding up to the parent: the parent's amount minus the
   * sum of its children, or null if they match or this isn't a split. Upstream stores this as the
   * transaction's `error` (a `SplitTransactionError`).
   */
  val splitDifference: Amount?
    get() {
      if (!isParent) return null
      val total = subtransactions.fold(Amount.Zero) { sum, child -> sum + child.amount }
      return (amount - total).takeIf { it != Zero }
    }
}
