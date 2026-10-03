package aktual.budget.transactions.ui

import aktual.budget.model.TransactionsDensity
import aktual.budget.transactions.vm.Transaction
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.scrollbar
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow

@Composable
internal fun Transactions(
  listState: LazyListState,
  pagingItems: LazyPagingItems<Transaction>,
  density: TransactionsDensity,
  innerPadding: PaddingValues,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  val refresh = pagingItems.loadState.refresh
  when {
    pagingItems.itemCount > 0 -> {
      TransactionsFilled(
        listState = listState,
        pagingItems = pagingItems,
        density = density,
        modifier = modifier,
        contentPadding = contentPadding,
        innerPadding = innerPadding,
      )
    }

    refresh is LoadState.Error -> {
      FailureScreen(
        modifier = modifier.padding(contentPadding),
        title = Strings.transactionsLoadFailed,
        reason = refresh.error.message,
        background = colors.tableBackground,
        action =
          FailureAction(
            text = { Strings.syncRetry },
            icon = MaterialIcons.Refresh,
            onClick = pagingItems::retry,
          ),
      )
    }

    refresh is LoadState.Loading -> {
      TransactionsLoading(density, modifier.padding(contentPadding))
    }

    else -> {
      TransactionsEmpty(density, modifier.padding(contentPadding))
    }
  }
}

@Composable
private fun TransactionsLoading(density: TransactionsDensity, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxSize()) {
    if (density == Dense) LedgerHeader()
    repeat(times = NUM_SHIMMER_ROWS) { LedgerShimmerRow() }
  }
}

@Composable
private fun TransactionsEmpty(density: TransactionsDensity, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxSize()) {
    if (density == Dense) LedgerHeader()

    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Center) {
      Text(
        text = Strings.transactionsEmpty,
        textAlign = Center,
        fontStyle = Italic,
        color = colors.tableText,
      )
    }
  }
}

@Composable
private fun TransactionsFilled(
  listState: LazyListState,
  pagingItems: LazyPagingItems<Transaction>,
  density: TransactionsDensity,
  innerPadding: PaddingValues,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
  ) {
    if (density == Dense) {
      stickyHeader {
        // Keep the sticky header below the top bar, but let transactions go underneath
        VerticalSpacer(innerPadding.calculateTopPadding())
        LedgerHeader()
      }
    }

    item { VerticalSpacer(innerPadding.calculateTopPadding()) }

    items(count = pagingItems.itemCount, key = pagingItems.itemKey { it.id.toString() }) { index ->
      val transaction = pagingItems[index]
      if (transaction != null) {
        val showDate = index == 0 || pagingItems.peek(index - 1)?.date != transaction.date

        Column(modifier = Modifier.fillMaxWidth().animateItem()) {
          // A hairline between days, or between every row when dense
          if (index > 0 && (showDate || density == Dense)) {
            HorizontalDivider(color = colors.tableBorder)
          }

          when (density) {
            Comfortable,
            Compact -> LedgerRow(transaction, showDate)
            Dense -> LedgerTableRow(transaction)
          }
        }
      }
    }

    item { BottomSpacing() }
  }
}

private const val NUM_SHIMMER_ROWS = 12

@PortraitPreview
@Composable
private fun PreviewTransactions(
  @PreviewParameter(TransactionsProvider::class) params: ColoredParams<TransactionsParams>
) =
  PreviewWithColors(params.colors) {
    WithLedgerDimens(params.data.density) {
      Transactions(
        listState = rememberLazyListState(),
        pagingItems = params.data.pagingData.collectAsLazyPagingItems(),
        density = params.data.density,
        contentPadding = Zero,
        innerPadding = Zero,
      )
    }
  }

private data class TransactionsParams(
  val density: TransactionsDensity,
  val pagingData: Flow<PagingData<Transaction>> = previewPagingData(PREVIEW_TRANSACTIONS),
)

private class TransactionsProvider :
  ColoredParameterProvider<TransactionsParams>(
    TransactionsParams(Comfortable),
    TransactionsParams(Compact),
    TransactionsParams(Dense),
    TransactionsParams(Compact, previewPagingData(persistentListOf())),
    TransactionsParams(Dense, previewPagingData(persistentListOf())),
    TransactionsParams(Compact, previewLoadingPagingData()),
    TransactionsParams(Dense, previewLoadingPagingData()),
  )
