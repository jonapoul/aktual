/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.budget.model.SortColumn.Amount
import aktual.budget.model.SortColumn.Date
import aktual.budget.model.SortDirection.Ascending
import aktual.budget.model.SortDirection.Descending
import aktual.budget.transactions.ui.CategoryHeader
import aktual.budget.transactions.vm.TransactionsSorting
import aktual.core.ui.PreviewThemedColumn
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
