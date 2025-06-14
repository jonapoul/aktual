package actual.budget.transactions.ui

import actual.budget.transactions.res.Strings
import actual.budget.transactions.vm.SortBy
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
  sorting: SortBy,
  onSort: (SortBy) -> Unit,
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
      sorting = sorting.toColumnSorting<SortBy.Date>(),
      onSort = { column -> onSort(SortBy.Date(column.toBoolean())) },
      theme = theme,
    )

    // account
    CategoryHeaderText(
      modifier = Modifier.weight(1f),
      text = Strings.transactionsHeaderAccount,
      sorting = sorting.toColumnSorting<SortBy.Account>(),
      onSort = { column -> onSort(SortBy.Account(column.toBoolean())) },
      theme = theme,
    )

    // payee
    CategoryHeaderText(
      modifier = Modifier.weight(1f),
      text = Strings.transactionsHeaderPayee,
      sorting = sorting.toColumnSorting<SortBy.Payee>(),
      onSort = { column -> onSort(SortBy.Payee(column.toBoolean())) },
      theme = theme,
    )

    // notes
    CategoryHeaderText(
      modifier = Modifier.weight(1f),
      text = Strings.transactionsHeaderNotes,
      sorting = sorting.toColumnSorting<SortBy.Notes>(),
      onSort = { column -> onSort(SortBy.Notes(column.toBoolean())) },
      theme = theme,
    )

    // category
    CategoryHeaderText(
      modifier = Modifier.weight(1f),
      text = Strings.transactionsHeaderCategory,
      sorting = sorting.toColumnSorting<SortBy.Category>(),
      onSort = { column -> onSort(SortBy.Category(column.toBoolean())) },
      theme = theme,
    )

    // amount
    CategoryHeaderText(
      modifier = Modifier.weight(1f),
      text = Strings.transactionsHeaderAmount,
      sorting = sorting.toColumnSorting<SortBy.Amount>(),
      onSort = { column -> onSort(SortBy.Amount(column.toBoolean())) },
      theme = theme,
    )

    // button
    Spacer(modifier = Modifier.width(LocalMinimumInteractiveComponentSize.current))
  }
}

@Stable
private inline fun <reified T : SortBy> SortBy.toColumnSorting() = if (T::class.isInstance(this)) {
  if (ascending) ColumnSorting.Ascending else ColumnSorting.Descending
} else {
  ColumnSorting.None
}

private enum class ColumnSorting {
  Ascending,
  Descending,
  None,
}

private fun ColumnSorting.toBoolean() = when (this) {
  ColumnSorting.Ascending -> true
  ColumnSorting.Descending -> false
  ColumnSorting.None -> error("Should never happen!")
}

@Composable
private fun CategoryHeaderText(
  text: String,
  sorting: ColumnSorting,
  onSort: (ColumnSorting) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier = modifier
      .clickable(
        indication = ripple(),
        interactionSource = remember { MutableInteractionSource() },
        onClick = {
          when (sorting) {
            ColumnSorting.Ascending -> onSort(ColumnSorting.Descending)
            ColumnSorting.Descending -> onSort(ColumnSorting.Ascending)
            ColumnSorting.None -> onSort(ColumnSorting.Descending)
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

    when (sorting) {
      ColumnSorting.Ascending -> Icon(
        modifier = Modifier.size(20.dp),
        imageVector = Icons.Filled.ArrowDownward,
        contentDescription = Strings.transactionsSortAsc,
        tint = theme.tableHeaderText,
      )

      ColumnSorting.Descending -> Icon(
        modifier = Modifier.size(20.dp),
        imageVector = Icons.Filled.ArrowUpward,
        contentDescription = Strings.transactionsSortDesc,
        tint = theme.tableHeaderText,
      )

      ColumnSorting.None -> Unit
    }
  }
}

@Preview
@Composable
private fun PreviewSortedByDate() = PreviewColumn {
  CategoryHeader(
    modifier = Modifier.fillMaxWidth(),
    sorting = SortBy.Date(ascending = false),
    onSort = {},
  )
}

@Preview
@Composable
private fun PreviewSortedByAmount() = PreviewColumn {
  CategoryHeader(
    modifier = Modifier.fillMaxWidth(),
    sorting = SortBy.Amount(ascending = true),
    onSort = {},
  )
}
