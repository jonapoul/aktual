package actual.budget.transactions.ui

import actual.budget.transactions.vm.DatedTransactions
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import my.nanihadesuka.compose.LazyColumnScrollbar

@Composable
internal fun Transactions(
  transactions: ImmutableList<DatedTransactions>,
  format: TransactionsFormat,
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
      transactions.fastForEachIndexed { index, (date, transactions) ->
        stickyHeader {
          DateHeader(
            modifier = Modifier.fillMaxWidth(),
            date = date,
            header = header,
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
        }
      }
    }
  }
}

@ScreenPreview
@Composable
private fun PreviewTransactions() = PreviewScreen {
  Transactions(
    checkbox = PreviewTransactionCheckbox,
    header = PreviewTransactionHeader,
    format = TransactionsFormat.Table,
    transactions = persistentListOf(
      DatedTransactions(TRANSACTION_1.date, persistentListOf(TRANSACTION_1, TRANSACTION_2)),
      DatedTransactions(TRANSACTION_3.date, persistentListOf(TRANSACTION_3)),
    ),
  )
}
