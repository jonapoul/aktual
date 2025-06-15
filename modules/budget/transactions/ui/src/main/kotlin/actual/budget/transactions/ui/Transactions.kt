package actual.budget.transactions.ui

import actual.budget.model.SortColumn
import actual.budget.model.SortDirection
import actual.budget.model.TransactionsFormat
import actual.budget.transactions.res.Strings
import actual.budget.transactions.vm.DatedTransactions
import actual.budget.transactions.vm.TransactionsSorting
import actual.core.res.CoreDimens
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.core.ui.scrollbarSettings
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
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import my.nanihadesuka.compose.LazyColumnScrollbar

@Composable
internal fun Transactions(
  transactions: ImmutableList<DatedTransactions>,
  format: TransactionsFormat,
  sorting: TransactionsSorting,
  provider: StateProvider,
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
      format = format,
      sorting = sorting,
      provider = provider,
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
  format: TransactionsFormat,
  sorting: TransactionsSorting,
  provider: StateProvider,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val listState = rememberLazyListState()
  LazyColumnScrollbar(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = CoreDimens.large),
    state = listState,
    settings = theme.scrollbarSettings(),
  ) {
    LazyColumn(
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

      transactions.fastForEach { (date, transactions) ->
        stickyHeader {
          DateHeader(
            modifier = Modifier.fillMaxWidth(),
            date = date,
            provider = provider,
            onAction = onAction,
            theme = theme,
          )
        }

        items(transactions) { transaction ->
          TransactionItem(
            modifier = Modifier.fillMaxWidth(),
            transaction = transaction,
            format = format,
            provider = provider,
            onAction = onAction,
            theme = theme,
          )

          HorizontalDivider(color = theme.tableBorderSeparator)
        }
      }
    }
  }
}

@ScreenPreview
@Composable
private fun PreviewTransactionsList() = PreviewScreen {
  Transactions(
    provider = StateProvider.Empty,
    onAction = {},
    format = TransactionsFormat.List,
    sorting = TransactionsSorting(SortColumn.Date, SortDirection.Ascending),
    transactions = persistentListOf(
      DatedTransactions(TRANSACTION_1.date, persistentListOf(TRANSACTION_1, TRANSACTION_2)),
      DatedTransactions(TRANSACTION_3.date, persistentListOf(TRANSACTION_3)),
    ),
  )
}

@ScreenPreview
@Composable
private fun PreviewTransactionsTable() = PreviewScreen {
  Transactions(
    provider = StateProvider.Empty,
    onAction = {},
    format = TransactionsFormat.Table,
    sorting = TransactionsSorting(SortColumn.Date, SortDirection.Ascending),
    transactions = persistentListOf(
      DatedTransactions(TRANSACTION_1.date, persistentListOf(TRANSACTION_1, TRANSACTION_2)),
      DatedTransactions(TRANSACTION_3.date, persistentListOf(TRANSACTION_3)),
    ),
  )
}

@ScreenPreview
@Composable
private fun PreviewTransactionsTableEmpty() = PreviewScreen {
  Transactions(
    provider = StateProvider.Empty,
    onAction = {},
    format = TransactionsFormat.Table,
    sorting = TransactionsSorting(SortColumn.Date, SortDirection.Ascending),
    transactions = persistentListOf(),
  )
}
