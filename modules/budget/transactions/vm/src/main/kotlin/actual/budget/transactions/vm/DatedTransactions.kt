package actual.budget.transactions.vm

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

@Immutable
data class DatedTransactions(
  val date: LocalDate,
  val transactions: ImmutableList<Transaction>,
)
