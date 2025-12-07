/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.transactions.ui

import aktual.budget.model.SortColumn
import aktual.budget.model.SortDirection
import aktual.budget.model.TransactionsFormat
import aktual.budget.transactions.vm.DatedTransactions
import aktual.budget.transactions.vm.TransactionsSorting
import aktual.core.ui.Dimens
import aktual.core.ui.LocalTheme
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.scrollbar
import aktual.l10n.Strings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun Transactions(
  transactions: ImmutableList<DatedTransactions>,
  observer: TransactionObserver,
  format: TransactionsFormat,
  sorting: TransactionsSorting,
  source: StateSource,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  if (transactions.isEmpty()) {
    TransactionsEmpty(
      sorting = sorting,
      modifier = modifier,
      theme = theme,
    )
  } else {
    TransactionsFilled(
      transactions = transactions,
      observer = observer,
      format = format,
      sorting = sorting,
      source = source,
      onAction = onAction,
      modifier = modifier,
      theme = theme,
    )
  }
}

@Composable
private fun TransactionsEmpty(
  sorting: TransactionsSorting,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier.fillMaxHeight(),
  ) {
    CategoryHeader(
      modifier = Modifier.fillMaxWidth(),
      sorting = sorting,
      onSort = {},
      theme = theme,
    )

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = Strings.transactionsEmpty,
        textAlign = TextAlign.Center,
        fontStyle = FontStyle.Italic,
        color = theme.tableText,
      )
    }
  }
}

@Composable
private fun TransactionsFilled(
  transactions: ImmutableList<DatedTransactions>,
  observer: TransactionObserver,
  format: TransactionsFormat,
  sorting: TransactionsSorting,
  source: StateSource,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val listState = rememberLazyListState()
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = Dimens.Large)
      .scrollbar(listState),
    state = listState,
  ) {
    if (format == TransactionsFormat.Table) {
      stickyHeader {
        CategoryHeader(
          modifier = Modifier.fillMaxWidth(),
          sorting = sorting,
          onSort = {},
          theme = theme,
        )
      }
    }

    transactions.fastForEach { (date, ids) ->
      stickyHeader {
        DateHeader(
          modifier = Modifier.fillMaxWidth(),
          date = date,
          source = source,
          onAction = onAction,
          theme = theme,
        )
      }

      items(ids) { id ->
        TransactionItem(
          modifier = Modifier.fillMaxWidth(),
          id = id,
          observer = observer,
          format = format,
          source = source,
          onAction = onAction,
          theme = theme,
        )

        HorizontalDivider(color = theme.tableBorderSeparator)
      }
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewTransactions(
  @PreviewParameter(TransactionsProvider::class) params: ThemedParams<TransactionsParams>,
) = PreviewWithColorScheme(params.type) {
  Transactions(
    transactions = params.data.transactions.toImmutableList(),
    format = params.data.format,
    source = params.data.source,
    sorting = params.data.sorting,
    observer = params.data.observer,
    onAction = {},
  )
}

private data class TransactionsParams(
  val sorting: TransactionsSorting,
  val observer: TransactionObserver,
  val format: TransactionsFormat,
  val source: StateSource = StateSource.Empty,
  val transactions: List<DatedTransactions>,
)

private class TransactionsProvider : ThemedParameterProvider<TransactionsParams>(
  TransactionsParams(
    sorting = TransactionsSorting(SortColumn.Date, SortDirection.Ascending),
    observer = previewObserver(TRANSACTION_1, TRANSACTION_2, TRANSACTION_3),
    format = TransactionsFormat.List,
    transactions = persistentListOf(
      DatedTransactions(TRANSACTION_1.date, persistentListOf(TRANSACTION_1.id, TRANSACTION_2.id)),
      DatedTransactions(TRANSACTION_3.date, persistentListOf(TRANSACTION_3.id)),
    ),
  ),
  TransactionsParams(
    sorting = TransactionsSorting(SortColumn.Date, SortDirection.Ascending),
    observer = previewObserver(TRANSACTION_1, TRANSACTION_2, TRANSACTION_3),
    format = TransactionsFormat.Table,
    transactions = persistentListOf(
      DatedTransactions(TRANSACTION_1.date, persistentListOf(TRANSACTION_1.id, TRANSACTION_2.id)),
      DatedTransactions(TRANSACTION_3.date, persistentListOf(TRANSACTION_3.id)),
    ),
  ),
  TransactionsParams(
    sorting = TransactionsSorting(SortColumn.Date, SortDirection.Ascending),
    observer = previewObserver(TRANSACTION_1, TRANSACTION_2, TRANSACTION_3),
    format = TransactionsFormat.Table,
    transactions = persistentListOf(),
  ),
)
