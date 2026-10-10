package aktual.budget.transactions.ui

import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsDensity
import aktual.budget.transactions.vm.Transaction
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.l10n.Strings
import aktual.core.theme.hasAlternateRowColour
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.scrollbar
import alakazam.compose.VerticalSpacer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.Flow

@Composable
internal fun Transactions(
  listState: LazyListState,
  pagingItems: LazyPagingItems<Transaction>,
  density: TransactionsDensity,
  innerPadding: PaddingValues,
  contentPadding: PaddingValues,
  expanded: ImmutableSet<TransactionId>,
  splitsPinnedOpen: Boolean,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  alternateRowColours: Boolean = false,
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
        expanded = expanded,
        splitsPinnedOpen = splitsPinnedOpen,
        alternateRowColours = alternateRowColours,
        onAction = onAction,
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

    refresh is Loading -> {
      TransactionsLoading(innerPadding, modifier.padding(contentPadding))
    }

    else -> {
      TransactionsEmpty(innerPadding, modifier.padding(contentPadding))
    }
  }
}

@Composable
private fun TransactionsLoading(innerPadding: PaddingValues, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxSize()) {
    VerticalSpacer(innerPadding.calculateTopPadding())

    repeat(times = NUM_SHIMMER_ROWS) { LedgerShimmerRow() }

    BottomSpacing()
  }
}

@Composable
private fun TransactionsEmpty(innerPadding: PaddingValues, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxSize()) {
    VerticalSpacer(innerPadding.calculateTopPadding())

    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Center) {
      Text(
        text = Strings.transactionsEmpty,
        textAlign = Center,
        fontStyle = Italic,
        color = colors.tableText,
      )
    }

    BottomSpacing()
  }
}

@Composable
private fun TransactionsFilled(
  listState: LazyListState,
  pagingItems: LazyPagingItems<Transaction>,
  density: TransactionsDensity,
  innerPadding: PaddingValues,
  contentPadding: PaddingValues,
  expanded: ImmutableSet<TransactionId>,
  splitsPinnedOpen: Boolean,
  alternateRowColours: Boolean,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) {
  // Only themes with their own alternate row colour shade rows, see hasAlternateRowColour
  val shadeRows = alternateRowColours && colors.hasAlternateRowColour

  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
  ) {
    item { VerticalSpacer(innerPadding.calculateTopPadding()) }

    items(count = pagingItems.itemCount, key = pagingItems.itemKey { it.id.toString() }) { index ->
      val transaction = pagingItems[index]
      if (transaction != null) {
        val previous = if (index > 0) pagingItems.peek(index - 1) else null
        val showDate = previous?.date != transaction.date
        val showYear = previous != null && previous.date.year != transaction.date.year

        val background =
          if (shadeRows && index % 2 == 1) {
            colors.tableRowBackgroundAlternate
          } else {
            colors.tableBackground
          }

        Column(modifier = Modifier.fillMaxWidth().animateItem().background(background)) {
          // A hairline between days, or between every row when dense
          if (index > 0 && (showDate || density == Dense)) {
            HorizontalDivider(color = colors.tableBorder)
          }

          // The first year is already pinned under the top bar
          if (showYear) YearDivider(transaction.date.year)

          val parts =
            when {
              splitsPinnedOpen -> SplitParts.Pinned
              transaction.id in expanded -> SplitParts.Expanded
              else -> SplitParts.Collapsed
            }
          val onToggleSplit = { onAction(Action.ToggleSplit(transaction.id)) }
          val onOpen = { onAction(Action.OpenTransaction(transaction.id)) }

          when (density) {
            Comfortable,
            Compact ->
              LedgerRow(
                transaction = transaction,
                showDate = showDate,
                parts = parts,
                onToggleSplit = onToggleSplit,
                onOpen = onOpen,
              )
            Dense ->
              LedgerTableRow(
                transaction = transaction,
                parts = parts,
                onToggleSplit = onToggleSplit,
                onOpen = onOpen,
              )
          }

          if (transaction.children.isNotEmpty()) {
            AnimatedVisibility(visible = parts != Collapsed) {
              SplitChildren(
                parent = transaction,
                density = density,
                onOpen = { id -> onAction(Action.OpenTransaction(id)) },
              )
            }
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
  @PreviewParameter(TransactionsProvider::class) params: ColoredParams<TransactionsParams>,
) =
  PreviewWithColoredParams(params) {
    WithLedgerDimens(density) {
      Transactions(
        listState = rememberLazyListState(),
        pagingItems = pagingData.collectAsLazyPagingItems(),
        density = density,
        contentPadding = Zero,
        innerPadding = Zero,
        expanded = persistentSetOf(TRANSACTION_SPLIT.id),
        splitsPinnedOpen = false,
        onAction = {},
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
    TransactionsParams(Comfortable, emptyPreviewPagingData(loading = true)),
    TransactionsParams(Compact, emptyPreviewPagingData(loading = true)),
    TransactionsParams(Dense, emptyPreviewPagingData(loading = true)),
    TransactionsParams(Compact, emptyPreviewPagingData(loading = false)),
    TransactionsParams(Dense, emptyPreviewPagingData(loading = false)),
  )
