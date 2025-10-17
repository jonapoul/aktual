/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
