package actual.budget.transactions.vm

import actual.budget.db.V_transactions
import actual.budget.model.Amount
import actual.budget.model.TransactionId
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable
data class Transaction(
  val id: TransactionId,
  val date: LocalDate,
  val account: String,
  val payee: String,
  val notes: String?,
  val category: String,
  val amount: Amount,
)

internal fun Transaction(entity: V_transactions, names: List<String>) = with(entity) {
  val (account, payee, category) = names
  Transaction(
    id = id,
    date = date,
    account = account,
    payee = payee,
    notes = notes,
    category = category,
    amount = Amount(amount),
  )
}
