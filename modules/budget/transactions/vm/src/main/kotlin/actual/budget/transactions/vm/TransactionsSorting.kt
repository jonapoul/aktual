package actual.budget.transactions.vm

import actual.budget.model.DbMetadata
import actual.budget.model.SortColumn
import actual.budget.model.SortDirection
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
  column = metadata[SortColumnDelegate],
  direction = metadata[SortDirectionDelegate],
)
