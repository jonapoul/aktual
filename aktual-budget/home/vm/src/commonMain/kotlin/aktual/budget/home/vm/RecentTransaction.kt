package aktual.budget.home.vm

import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

// Which way the money went, from the side of the transaction's account
enum class TransferDirection {
  To,
  From,
}

// On a transfer, payee is the name of the other account
@Immutable
data class RecentTransaction(
  val id: TransactionId,
  val date: LocalDate,
  val payee: String?,
  val transfer: TransferDirection?,
  val account: String?,
  val category: String?,
  val amount: Amount,
  val needsCategory: Boolean,
  val isSplit: Boolean,
)
