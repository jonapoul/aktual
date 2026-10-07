package aktual.budget.transactions.vm

import aktual.budget.db.dao.TransactionPage
import aktual.budget.db.dao.TransactionRow
import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate

enum class SplitRole {
  None,
  Parent,
  Child,
}

@Immutable
data class Transaction(
  val id: TransactionId,
  val date: LocalDate,
  val account: String?,
  val payee: String?,
  val notes: String?,
  val category: String?,
  val amount: Amount,
  val balance: Amount?,
  val needsCategory: Boolean = false,
  val split: SplitRole = None,
  // The parts to show under a parent, which in a tag list can be fewer than totalChildren
  val children: ImmutableList<Transaction> = persistentListOf(),
  val totalChildren: Int = 0,
) : Comparable<Transaction> {
  override fun compareTo(other: Transaction) = date.compareTo(other.date)
}

// Walks down the page from the balance after its first row. Split children hang off their parent
// with no balance, since its amount already covers theirs.
internal fun TransactionPage.toTransactions(
  children: Map<TransactionId, List<TransactionRow>>
): List<Transaction> {
  var balance = topBalance
  return rows.map { row ->
    row.toTransaction(Amount(balance), children[row.id].orEmpty()).also { balance -= row.amount }
  }
}

// A null shownChildren shows every child
internal fun TransactionRow.toTransaction(
  balance: Amount?,
  children: List<TransactionRow> = emptyList(),
  shownChildren: Set<TransactionId>? = null,
): Transaction =
  Transaction(
    id = id,
    date = date,
    account = accountName,
    payee = if (isParent) displayPayee(children) else payeeName,
    notes = notes,
    category = categoryName,
    amount = Amount(amount),
    balance = balance,
    needsCategory = needsCategory,
    split =
      when {
        isParent -> Parent
        isChild == true -> Child
        else -> None
      },
    children =
      children
        .filter { shownChildren == null || it.id in shownChildren }
        .map { it.toTransaction(balance = null) }
        .toImmutableList(),
    totalChildren = children.size,
  )

// A split shows the most common payee of its parts, the first to get there on a tie, and never its
// own. Mirrors packages/desktop-client/src/hooks/useDisplayPayee.tsx
internal fun displayPayee(children: List<TransactionRow>): String? {
  val counts = mutableMapOf<String, Int>()
  var mostCommon: String? = null
  var maxCount = 0
  for (payee in children.mapNotNull { it.payeeName }) {
    val count = counts.getOrElse(payee) { 0 } + 1
    counts[payee] = count
    if (count > maxCount) {
      maxCount = count
      mostCommon = payee
    }
  }
  return mostCommon
}
