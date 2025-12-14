package aktual.budget.transactions.ui

import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsFormat
import aktual.budget.model.TransactionsFormat.List
import aktual.budget.model.TransactionsFormat.Table
import aktual.budget.transactions.vm.Transaction
import aktual.budget.transactions.vm.TransactionState
import aktual.budget.transactions.vm.TransactionStateSource
import aktual.core.ui.BareIconButton
import aktual.core.ui.CardShape
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.TabletPreview
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.aktualHaze
import aktual.core.ui.formattedString
import aktual.l10n.Strings
import alakazam.kotlin.compose.HorizontalSpacer
import alakazam.kotlin.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.unclippedBoundsInWindow

@Composable
internal fun TransactionItem(
  id: TransactionId,
  format: TransactionsFormat,
  source: TransactionStateSource,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val state by source
    .transactionState(id)
    .collectAsStateWithLifecycle(TransactionState.Loading(id))

  TransactionItem(
    state = state,
    format = format,
    source = source,
    onAction = onAction,
    modifier = modifier,
    theme = theme,
  )
}

@Composable
private fun TransactionItem(
  state: TransactionState,
  format: TransactionsFormat,
  source: TransactionStateSource,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = when (state) {
  is TransactionState.Loading -> LoadingItem(format, modifier, theme)
  is TransactionState.Loaded -> LoadedItem(state.transaction, format, source, onAction, modifier, theme)
  is TransactionState.DoesntExist -> FailedItem(state.id, modifier, theme)
}

private val ShimmerShape = RoundedCornerShape(4.dp)
private val ShimmerRowHeight = 16.dp

@Composable
private fun LoadingItem(
  format: TransactionsFormat,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val dimens = LocalTableDimens.current
  val shimmer = rememberShimmer(ShimmerBounds.Custom)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(LocalMinimumInteractiveComponentSize.current)
      .clip(CardShape)
      .padding(vertical = dimens.rowVertical, horizontal = dimens.rowHorizontal)
      .shimmer(shimmer)
      .aktualHaze()
      .onGloballyPositioned { layoutCoordinates ->
        val position = layoutCoordinates.unclippedBoundsInWindow()
        shimmer.updateBounds(position)
      },
    verticalAlignment = Alignment.CenterVertically,
  ) {
    when (format) {
      List -> LoadingTransactionListItem(dimens, theme)
      Table -> LoadingTransactionTableItem(dimens, theme)
    }
  }
}

@Composable
@Suppress("MagicNumber")
private fun RowScope.LoadingTransactionListItem(
  dimens: TransactionSpacings,
  theme: Theme,
) {
  // Checkbox placeholder
  Box(
    modifier = Modifier.minimumInteractiveComponentSize(),
  )

  HorizontalSpacer(dimens.interColumn)

  // Main content placeholder
  Column(
    modifier = Modifier.weight(1f),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.6f)
        .height(ShimmerRowHeight)
        .background(theme.tableText, ShimmerShape),
    )

    VerticalSpacer(4.dp)

    Box(
      modifier = Modifier
        .fillMaxWidth(0.8f)
        .height(ShimmerRowHeight)
        .background(theme.tableText, ShimmerShape),
    )
  }

  HorizontalSpacer(dimens.interColumn)

  // Amount placeholder
  Box(
    modifier = Modifier
      .width(80.dp)
      .height(ShimmerRowHeight)
      .background(theme.tableText, ShimmerShape),
  )

  HorizontalSpacer(dimens.interColumn)

  // Menu button placeholder
  Box(
    modifier = Modifier.minimumInteractiveComponentSize(),
  )
}

@Stable
@Suppress("MagicNumber")
private val TransactionSpacings.loadingInterColumn: Dp get() = interColumn * 10

@Composable
private fun RowScope.LoadingTransactionTableItem(
  dimens: TransactionSpacings,
  theme: Theme,
) {
  // Checkbox placeholder
  Box(
    modifier = Modifier.minimumInteractiveComponentSize(),
  )

  HorizontalSpacer(dimens.loadingInterColumn)

  Box(
    modifier = Modifier
      .weight(1f)
      .height(ShimmerRowHeight)
      .background(theme.tableText, ShimmerShape),
  )

  HorizontalSpacer(dimens.loadingInterColumn)

  Box(
    modifier = Modifier
      .weight(1f)
      .height(ShimmerRowHeight)
      .background(theme.tableText, ShimmerShape),
  )

  HorizontalSpacer(dimens.loadingInterColumn)

  Box(
    modifier = Modifier
      .weight(1f)
      .height(ShimmerRowHeight)
      .background(theme.tableText, ShimmerShape),
  )

  HorizontalSpacer(dimens.loadingInterColumn)

  Box(
    modifier = Modifier
      .weight(1f)
      .height(ShimmerRowHeight)
      .background(theme.tableText, ShimmerShape),
  )

  HorizontalSpacer(dimens.loadingInterColumn)

  Box(
    modifier = Modifier
      .weight(1f)
      .height(ShimmerRowHeight)
      .background(theme.tableText, ShimmerShape),
  )

  HorizontalSpacer(dimens.loadingInterColumn)

  Box(
    modifier = Modifier
      .weight(1f)
      .height(ShimmerRowHeight)
      .background(theme.tableText, ShimmerShape),
  )

  HorizontalSpacer(dimens.loadingInterColumn)

  // Menu button placeholder
  Box(
    modifier = Modifier.minimumInteractiveComponentSize(),
  )
}

@Composable
private fun FailedItem(
  id: TransactionId,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val dimens = LocalTableDimens.current
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(LocalMinimumInteractiveComponentSize.current)
      .clip(CardShape)
      .aktualHaze()
      .padding(vertical = dimens.rowVertical, horizontal = dimens.rowHorizontal),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Checkbox placeholder
    Box(
      modifier = Modifier.minimumInteractiveComponentSize(),
    )

    Text(
      modifier = Modifier.weight(1f),
      text = Strings.transactionsItemNotFound(id.toString()),
      color = theme.errorText,
      maxLines = 3,
    )

    // Button placeholder
    Box(
      modifier = Modifier.minimumInteractiveComponentSize(),
    )
  }
}

@Composable
private fun LoadedItem(
  transaction: Transaction,
  format: TransactionsFormat,
  source: TransactionStateSource,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val dimens = LocalTableDimens.current
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(IntrinsicSize.Min)
      .clip(CardShape)
      .aktualHaze()
      .padding(vertical = dimens.rowVertical, horizontal = dimens.rowHorizontal),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    when (format) {
      List -> TransactionListItem(
        transaction = transaction,
        source = source,
        onAction = onAction,
        theme = theme,
        dimens = dimens,
      )

      Table -> TransactionTableItem(
        transaction = transaction,
        source = source,
        onAction = onAction,
        theme = theme,
        dimens = dimens,
      )
    }
  }
}

@Composable
private fun RowScope.TransactionListItem(
  transaction: Transaction,
  source: TransactionStateSource,
  onAction: ActionListener,
  theme: Theme,
  dimens: TransactionSpacings,
) {
  val isChecked by source.isChecked(transaction.id).collectAsStateWithLifecycle(initialValue = false)
  Checkbox(
    modifier = Modifier.minimumInteractiveComponentSize(),
    checked = isChecked,
    onCheckedChange = { newValue -> onAction(Action.CheckItem(transaction.id, newValue)) },
  )

  HorizontalSpacer(dimens.interColumn)

  Column(
    modifier = Modifier.weight(1f),
  ) {
    Text(
      text = transaction.account.orEmpty(),
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      textAlign = TextAlign.Start,
      fontSize = dimens.textSize,
      color = theme.tableText,
    )

    Text(
      text = transaction.payee.orEmpty(),
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      textAlign = TextAlign.Start,
      fontSize = dimens.textSize,
      color = theme.tableText,
    )

    Text(
      text = transaction.notes.orEmpty(),
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      textAlign = TextAlign.Start,
      fontSize = dimens.textSize,
      color = theme.tableText,
    )

    Text(
      text = transaction.category.orEmpty(),
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      textAlign = TextAlign.Start,
      fontSize = dimens.textSize,
      color = theme.tableText,
    )
  }

  Text(
    text = transaction.amount.formattedString(),
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
    textAlign = TextAlign.End,
    fontSize = dimens.textSize,
    color = theme.tableText,
  )

  HorizontalSpacer(dimens.interColumn)

  BareIconButton(
    modifier = Modifier.minimumInteractiveComponentSize(),
    imageVector = Icons.Filled.MoreVert,
    contentDescription = "",
    onClick = {},
  )
}

@Composable
private fun RowScope.TransactionTableItem(
  transaction: Transaction,
  source: TransactionStateSource,
  onAction: ActionListener,
  theme: Theme,
  dimens: TransactionSpacings,
) {
  val isChecked by source.isChecked(transaction.id).collectAsStateWithLifecycle(initialValue = false)
  Checkbox(
    modifier = Modifier.minimumInteractiveComponentSize(),
    checked = isChecked,
    onCheckedChange = { newValue -> onAction(Action.CheckItem(transaction.id, newValue)) },
  )

  HorizontalSpacer(dimens.interColumn)

  Text(
    modifier = Modifier.weight(1f),
    text = transaction.date.toString(),
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
    textAlign = TextAlign.Start,
    fontSize = dimens.textSize,
    color = theme.tableText,
  )

  HorizontalSpacer(dimens.interColumn)

  Text(
    modifier = Modifier.weight(1f),
    text = transaction.account.orEmpty(),
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
    textAlign = TextAlign.Start,
    fontSize = dimens.textSize,
    color = theme.tableText,
  )

  HorizontalSpacer(dimens.interColumn)

  Text(
    modifier = Modifier.weight(1f),
    text = transaction.payee.orEmpty(),
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
    textAlign = TextAlign.Start,
    fontSize = dimens.textSize,
    color = theme.tableText,
  )

  HorizontalSpacer(dimens.interColumn)

  Text(
    modifier = Modifier.weight(1f),
    text = transaction.notes.orEmpty(),
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
    textAlign = TextAlign.Start,
    fontSize = dimens.textSize,
    color = theme.tableText,
  )

  HorizontalSpacer(dimens.interColumn)

  Text(
    modifier = Modifier.weight(1f),
    text = transaction.category.orEmpty(),
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
    textAlign = TextAlign.Start,
    fontSize = dimens.textSize,
    color = theme.tableText,
  )

  HorizontalSpacer(dimens.interColumn)

  Text(
    modifier = Modifier.weight(1f),
    text = transaction.amount.formattedString(),
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
    textAlign = TextAlign.End,
    fontSize = dimens.textSize,
    color = theme.tableText,
  )

  HorizontalSpacer(dimens.interColumn)

  BareIconButton(
    modifier = Modifier.minimumInteractiveComponentSize(),
    imageVector = Icons.Filled.MoreVert,
    contentDescription = "",
    onClick = {},
  )
}

@Preview
@Composable
private fun PreviewListFormat(
  @PreviewParameter(TransactionItemProvider::class) params: ThemedParams<TransactionItemParams>,
) = PreviewWithColorScheme(params.type) {
  TransactionItem(
    state = params.data.state,
    format = List,
    source = PreviewTransactionStateSource(params.data.state to params.data.isChecked),
    onAction = {},
  )
}

@TabletPreview
@Composable
private fun PreviewTableFormat(
  @PreviewParameter(TransactionItemProvider::class) params: ThemedParams<TransactionItemParams>,
) = PreviewWithColorScheme(params.type) {
  TransactionItem(
    state = params.data.state,
    format = Table,
    source = PreviewTransactionStateSource(params.data.state to params.data.isChecked),
    onAction = {},
  )
}

private data class TransactionItemParams(
  val state: TransactionState,
  val isChecked: Boolean,
)

private class TransactionItemProvider : ThemedParameterProvider<TransactionItemParams>(
  TransactionItemParams(TransactionState.Loaded(TRANSACTION_1), isChecked = false),
  TransactionItemParams(TransactionState.Loaded(TRANSACTION_1), isChecked = true),
  TransactionItemParams(TransactionState.Loading(id = TransactionId("abc")), isChecked = false),
  TransactionItemParams(TransactionState.DoesntExist(id = TransactionId("abc")), isChecked = false),
)
