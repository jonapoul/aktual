package aktual.budget.transactions.vm

import aktual.budget.model.DbMetadata
import aktual.budget.model.SortColumn
import aktual.budget.model.SortDirection
import androidx.compose.runtime.Immutable

@Immutable
data class TransactionsSorting(
  val column: SortColumn,
  val direction: SortDirection,
) {
  companion object {
    val Default = TransactionsSorting(SortColumn.Default, SortDirection.Default)
  }
}

fun TransactionsSorting(metadata: DbMetadata) = TransactionsSorting(
  column = metadata[SortColumnKey] ?: SortColumn.Default,
  direction = metadata[SortDirectionKey] ?: SortDirection.Default,
)
