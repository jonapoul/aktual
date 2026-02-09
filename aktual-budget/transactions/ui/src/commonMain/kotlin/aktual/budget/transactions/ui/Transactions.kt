package aktual.budget.transactions.ui

import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsFormat
import aktual.budget.model.TransactionsFormat.Table
import aktual.budget.transactions.vm.Transaction
import aktual.budget.transactions.vm.TransactionIdSource
import aktual.budget.transactions.vm.TransactionStateSource
import aktual.core.l10n.Strings
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.Dimens
import aktual.core.ui.LocalTheme
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.TabletPreview
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.scrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey

@Composable
internal fun Transactions(
    transactionIdSource: TransactionIdSource,
    format: TransactionsFormat,
    source: TransactionStateSource,
    onAction: ActionListener,
    modifier: Modifier = Modifier,
    theme: Theme = LocalTheme.current,
) {
  val pagingItems = transactionIdSource.pagingData.collectAsLazyPagingItems()
  if (pagingItems.itemCount == 0) {
    TransactionsEmpty(
        modifier = modifier,
        theme = theme,
    )
  } else {
    TransactionsFilled(
        pagingItems = pagingItems,
        format = format,
        source = source,
        onAction = onAction,
        modifier = modifier,
        theme = theme,
    )
  }
}

@Composable
private fun TransactionsEmpty(
    modifier: Modifier = Modifier,
    theme: Theme = LocalTheme.current,
) {
  Column(
      modifier = modifier.fillMaxHeight(),
  ) {
    CategoryHeader(
        modifier = Modifier.fillMaxWidth(),
        theme = theme,
    )

    Box(
        modifier = Modifier.fillMaxWidth().weight(1f),
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
    pagingItems: LazyPagingItems<TransactionId>,
    format: TransactionsFormat,
    source: TransactionStateSource,
    onAction: ActionListener,
    modifier: Modifier = Modifier,
    theme: Theme = LocalTheme.current,
) {
  val listState = rememberLazyListState()
  LazyColumn(
      modifier = modifier.fillMaxSize().padding(horizontal = Dimens.Large).scrollbar(listState),
      state = listState,
      verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
  ) {
    if (format == Table) {
      stickyHeader {
        CategoryHeader(
            modifier = Modifier.fillMaxWidth(),
            theme = theme,
        )
      }
    }

    items(
        count = pagingItems.itemCount,
        key = pagingItems.itemKey { it.toString() },
    ) { index ->
      val id = pagingItems[index]
      if (id != null) {
        TransactionItem(
            modifier = Modifier.fillMaxWidth(),
            id = id,
            format = format,
            source = source,
            onAction = onAction,
            theme = theme,
        )
      }
    }

    item {
      BottomStatusBarSpacing()
      BottomNavBarSpacing()
    }
  }
}

@PortraitPreview
@TabletPreview
@Composable
private fun PreviewTransactions(
    @PreviewParameter(TransactionsProvider::class) params: ThemedParams<TransactionsParams>,
) =
    PreviewWithColorScheme(params.type) {
      Transactions(
          transactionIdSource = PreviewTransactionIdSource(params.data.transactions),
          format = params.data.format,
          source = previewTransactionStateSource(params.data.transactions),
          onAction = {},
      )
    }

private data class TransactionsParams(
    val format: TransactionsFormat,
    val transactions: List<Transaction> = emptyList(),
)

private class TransactionsProvider :
    ThemedParameterProvider<TransactionsParams>(
        TransactionsParams(
            format = TransactionsFormat.List,
            transactions = listOf(TRANSACTION_1, TRANSACTION_2, TRANSACTION_3),
        ),
        TransactionsParams(
            format = Table,
            transactions = listOf(TRANSACTION_1, TRANSACTION_2, TRANSACTION_3),
        ),
        TransactionsParams(Table),
        TransactionsParams(TransactionsFormat.List),
    )
