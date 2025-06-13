package actual.budget.transactions.vm

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
