package aktual.budget.transactions.vm

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

// Dummy running balance until #1675 computes the real one
internal val DummyBalance = Amount(3412.60)

internal fun TransactionRow.toTransaction() =
  Transaction(
    id = id,
    date = date,
    account = accountName,
    payee = payeeName,
    notes = notes,
    category = categoryName,
    amount = Amount(amount),
    balance = DummyBalance,
  )
