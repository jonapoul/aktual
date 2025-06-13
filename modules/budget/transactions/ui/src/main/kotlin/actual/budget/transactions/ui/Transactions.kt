package actual.budget.transactions.ui

import actual.budget.transactions.vm.DatedTransactions
import actual.budget.transactions.vm.SortBy
import actual.budget.transactions.vm.TransactionsFormat
import actual.core.res.CoreDimens
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.core.ui.scrollbarSettings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import my.nanihadesuka.compose.LazyColumnScrollbar

@Composable
internal fun Transactions(
  transactions: ImmutableList<DatedTransactions>,
  format: TransactionsFormat,
  sorting: SortBy,
  checkbox: TransactionCheckbox,
  header: TransactionHeader,
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
            header = header,
            theme = theme,
          )
        }

        items(transactions) { transaction ->
          TransactionItem(
            modifier = Modifier.fillMaxWidth(),
            transaction = transaction,
            format = format,
            checkbox = checkbox,
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
    checkbox = PreviewTransactionCheckbox,
    header = PreviewTransactionHeader,
    format = TransactionsFormat.List,
    sorting = SortBy.Date(ascending = true),
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
    checkbox = PreviewTransactionCheckbox,
    header = PreviewTransactionHeader,
    format = TransactionsFormat.Table,
    sorting = SortBy.Date(ascending = true),
    transactions = persistentListOf(
      DatedTransactions(TRANSACTION_1.date, persistentListOf(TRANSACTION_1, TRANSACTION_2)),
      DatedTransactions(TRANSACTION_3.date, persistentListOf(TRANSACTION_3)),
    ),
  )
}
