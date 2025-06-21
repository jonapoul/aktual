package actual.budget.transactions.ui

import actual.budget.model.SortColumn
import actual.budget.model.SortColumn.Account
import actual.budget.model.SortColumn.Amount
import actual.budget.model.SortColumn.Category
import actual.budget.model.SortColumn.Date
import actual.budget.model.SortColumn.Notes
import actual.budget.model.SortColumn.Payee
import actual.budget.model.SortDirection
import actual.budget.model.SortDirection.Ascending
import actual.budget.model.SortDirection.Descending
import actual.budget.transactions.res.Strings
import actual.budget.transactions.vm.TransactionsSorting
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
      .clickable(
        indication = ripple(),
        interactionSource = remember { MutableInteractionSource() },
        onClick = {
          when (direction) {
            Ascending -> onSort(Descending)
            Descending -> onSort(Ascending)
            null -> onSort(Descending)
          }
        },
      ),
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

@Preview
@Composable
private fun PreviewSortedByDate() = PreviewColumn {
  CategoryHeader(
    modifier = Modifier.fillMaxWidth(),
    sorting = TransactionsSorting(column = Date, direction = Descending),
    onSort = {},
  )
}

@Preview
@Composable
private fun PreviewSortedByAmount() = PreviewColumn {
  CategoryHeader(
    modifier = Modifier.fillMaxWidth(),
    sorting = TransactionsSorting(column = Amount, direction = Ascending),
    onSort = {},
  )
}
