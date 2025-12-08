package aktual.budget.transactions.vm

import aktual.budget.model.TransactionId
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

@Immutable
data class DatedTransactions(
  val date: LocalDate,
  val ids: ImmutableList<TransactionId>,
)
