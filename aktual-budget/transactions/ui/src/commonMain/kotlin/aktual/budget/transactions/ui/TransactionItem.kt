package aktual.budget.transactions.ui

import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsFormat
import aktual.budget.model.TransactionsFormat.List
import aktual.budget.model.TransactionsFormat.Table
import aktual.budget.transactions.vm.Transaction
import aktual.budget.transactions.vm.TransactionState
import aktual.budget.transactions.vm.TransactionStateSource
import aktual.core.ui.BareIconButton
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.TabletPreview
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.formattedString
import aktual.l10n.Strings
import alakazam.kotlin.compose.HorizontalSpacer
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
  is TransactionState.Loading -> LoadingTransactionItem(modifier, theme)
  is TransactionState.Loaded -> LoadedTransactionItem(state.transaction, format, source, onAction, modifier, theme)
  is TransactionState.DoesntExist -> FailedTransactionItem(state.id, modifier, theme)
}

@Composable
private fun LoadingTransactionItem(
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val dimens = LocalTableDimens.current
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(LocalMinimumInteractiveComponentSize.current)
      .background(theme.tableBackground)
      .padding(vertical = dimens.rowVertical, horizontal = dimens.rowHorizontal),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Checkbox placeholder
    Box(
      modifier = Modifier
        .minimumInteractiveComponentSize()
        .shimmer(theme)
    )

    HorizontalSpacer(dimens.interColumn)

    // Main content placeholder
    Column(
      modifier = Modifier.weight(1f),
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(0.6f)
          .height(16.dp)
          .shimmer(theme)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth(0.8f)
          .height(16.dp)
          .shimmer(theme)
      )
    }

    HorizontalSpacer(dimens.interColumn)

    // Amount placeholder
    Box(
      modifier = Modifier
        .width(80.dp)
        .height(16.dp)
        .shimmer(theme)
    )

    HorizontalSpacer(dimens.interColumn)

    // Menu button placeholder
    Box(
      modifier = Modifier
        .minimumInteractiveComponentSize()
        .shimmer(theme)
    )
  }
}

@Composable
private fun FailedTransactionItem(
  id: TransactionId,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val dimens = LocalTableDimens.current
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(LocalMinimumInteractiveComponentSize.current)
      .background(theme.tableBackground)
      .padding(vertical = dimens.rowVertical, horizontal = dimens.rowHorizontal),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Checkbox placeholder
    Box(
      modifier = Modifier
        .minimumInteractiveComponentSize()
        .shimmer(theme)
    )

    Text(
      modifier = Modifier.weight(1f),
      text = Strings.transactionsItemNotFound(id.toString()),
      color = theme.errorText,
      fontWeight = FontWeight.Bold,
      maxLines = 3,
    )

    // Button placeholder
    Box(
      modifier = Modifier
        .minimumInteractiveComponentSize()
        .shimmer(theme)
    )
  }
}

private fun Modifier.shimmer(theme: Theme): Modifier = composed {
  val transition = rememberInfiniteTransition(label = "shimmer")
  val translateAnim by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "shimmer-translate"
  )

  val shimmerColors = listOf(
    theme.tableText.copy(alpha = 0.1f),
    theme.tableText.copy(alpha = 0.2f),
    theme.tableText.copy(alpha = 0.1f),
  )

  val brush = Brush.linearGradient(
    colors = shimmerColors,
    start = Offset(translateAnim, translateAnim),
    end = Offset(translateAnim + 200f, translateAnim + 200f)
  )

  background(brush = brush, shape = RoundedCornerShape(4.dp))
}

@Composable
private fun LoadedTransactionItem(
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
      .background(theme.tableBackground)
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
      text = transaction.account.orEmptyString(),
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      textAlign = TextAlign.Start,
      fontSize = dimens.textSize,
      color = theme.tableText,
    )

    Text(
      text = transaction.payee.orEmptyString(),
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
      text = transaction.category.orEmptyString(),
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
    text = transaction.date.toString().orEmptyString(),
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
    textAlign = TextAlign.Start,
    fontSize = dimens.textSize,
    color = theme.tableText,
  )

  HorizontalSpacer(dimens.interColumn)

  Text(
    modifier = Modifier.weight(1f),
    text = transaction.account.orEmptyString(),
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
    textAlign = TextAlign.Start,
    fontSize = dimens.textSize,
    color = theme.tableText,
  )

  HorizontalSpacer(dimens.interColumn)

  Text(
    modifier = Modifier.weight(1f),
    text = transaction.payee.orEmptyString(),
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
    text = transaction.category.orEmptyString(),
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

@Stable
@Composable
@ReadOnlyComposable
private fun String?.orEmptyString() = this ?: ""

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
