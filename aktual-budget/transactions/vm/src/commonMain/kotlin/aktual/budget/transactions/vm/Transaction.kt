package aktual.budget.transactions.vm

import aktual.budget.db.dao.TransactionPage
import aktual.budget.db.dao.TransactionRow
import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

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
) : Comparable<Transaction> {
  override fun compareTo(other: Transaction) = date.compareTo(other.date)
}

// Walks down the page from the balance after its first row. Split children show none, since their
// parent's amount already covers theirs.
internal fun TransactionPage.toTransactions(): List<Transaction> {
  var balance = topBalance
  return rows.map { row ->
    if (row.isChild == true) {
      row.toTransaction(balance = null)
    } else {
      row.toTransaction(Amount(balance)).also { balance -= row.amount }
    }
  }
}

internal fun TransactionRow.toTransaction(balance: Amount?) =
  Transaction(
    id = id,
    date = date,
    account = accountName,
    payee = payeeName,
    notes = notes,
    category = categoryName,
    amount = Amount(amount),
    balance = balance,
  )
