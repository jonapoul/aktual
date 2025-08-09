package actual.preview

import actual.budget.model.SortColumn.Amount
import actual.budget.model.SortColumn.Date
import actual.budget.model.SortDirection.Ascending
import actual.budget.model.SortDirection.Descending
import actual.budget.transactions.ui.CategoryHeader
import actual.budget.transactions.vm.TransactionsSorting
import actual.core.ui.PreviewThemedColumn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewSortedByDate() = PreviewThemedColumn {
  CategoryHeader(
    modifier = Modifier.fillMaxWidth(),
    sorting = TransactionsSorting(column = Date, direction = Descending),
    onSort = {},
  )
}

@Preview
@Composable
private fun PreviewSortedByAmount() = PreviewThemedColumn {
  CategoryHeader(
    modifier = Modifier.fillMaxWidth(),
    sorting = TransactionsSorting(column = Amount, direction = Ascending),
    onSort = {},
  )
}
