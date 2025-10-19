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
package aktual.budget.transactions.ui

import aktual.budget.model.SortColumn
import aktual.budget.model.SortColumn.Account
import aktual.budget.model.SortColumn.Amount
import aktual.budget.model.SortColumn.Category
import aktual.budget.model.SortColumn.Date
import aktual.budget.model.SortColumn.Notes
import aktual.budget.model.SortColumn.Payee
import aktual.budget.model.SortDirection
import aktual.budget.model.SortDirection.Ascending
import aktual.budget.model.SortDirection.Descending
import aktual.budget.transactions.vm.TransactionsSorting
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import aktual.l10n.Strings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun CategoryHeader(
  sorting: TransactionsSorting,
  onSort: (TransactionsSorting) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier = modifier
      .heightIn(min = LocalMinimumInteractiveComponentSize.current)
      .fillMaxWidth()
      .background(theme.tableHeaderBackground),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // checkbox
    CategoryHeaderText(
      modifier = Modifier.width(LocalMinimumInteractiveComponentSize.current),
      text = Strings.transactionsHeaderDate,
      direction = sorting.toDirection(Date),
      onSort = { dir -> onSort(TransactionsSorting(Date, dir)) },
      theme = theme,
    )

    // account
    CategoryHeaderText(
      modifier = Modifier.weight(1f),
      text = Strings.transactionsHeaderAccount,
      direction = sorting.toDirection(Account),
      onSort = { dir -> onSort(TransactionsSorting(Account, dir)) },
      theme = theme,
    )

    // payee
    CategoryHeaderText(
      modifier = Modifier.weight(1f),
      text = Strings.transactionsHeaderPayee,
      direction = sorting.toDirection(Payee),
      onSort = { dir -> onSort(TransactionsSorting(Payee, dir)) },
      theme = theme,
    )

    // notes
    CategoryHeaderText(
      modifier = Modifier.weight(1f),
      text = Strings.transactionsHeaderNotes,
      direction = sorting.toDirection(Notes),
      onSort = { dir -> onSort(TransactionsSorting(Notes, dir)) },
      theme = theme,
    )

    // category
    CategoryHeaderText(
      modifier = Modifier.weight(1f),
      text = Strings.transactionsHeaderCategory,
      direction = sorting.toDirection(Category),
      onSort = { dir -> onSort(TransactionsSorting(Category, dir)) },
      theme = theme,
    )

    // amount
    CategoryHeaderText(
      modifier = Modifier.weight(1f),
      text = Strings.transactionsHeaderAmount,
      direction = sorting.toDirection(Amount),
      onSort = { dir -> onSort(TransactionsSorting(Amount, dir)) },
      theme = theme,
    )

    // button
    Spacer(modifier = Modifier.width(LocalMinimumInteractiveComponentSize.current))
  }
}

@Stable
private fun TransactionsSorting.toDirection(target: SortColumn): SortDirection? =
  if (column == target) direction else null

@Composable
private fun CategoryHeaderText(
  text: String,
  direction: SortDirection?,
  onSort: (SortDirection) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier = modifier
      .clickable {
        when (direction) {
          Ascending -> onSort(Descending)
          Descending -> onSort(Ascending)
          null -> onSort(Descending)
        }
      },
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.End,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = text,
      textAlign = TextAlign.Center,
      fontSize = 14.sp,
      color = theme.tableHeaderText,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
    )

    when (direction) {
      Ascending -> Icon(
        modifier = Modifier.size(20.dp),
        imageVector = Icons.Filled.ArrowDownward,
        contentDescription = Strings.transactionsSortAsc,
        tint = theme.tableHeaderText,
      )

      Descending -> Icon(
        modifier = Modifier.size(20.dp),
        imageVector = Icons.Filled.ArrowUpward,
        contentDescription = Strings.transactionsSortDesc,
        tint = theme.tableHeaderText,
      )

      null -> Unit
    }
  }
}
