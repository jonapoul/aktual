/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.transactions.ui

import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsFormat
import aktual.budget.transactions.vm.Transaction
import aktual.core.ui.BareIconButton
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import aktual.core.ui.formattedString
import aktual.l10n.Strings
import alakazam.kotlin.compose.HorizontalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun TransactionItem(
  id: TransactionId,
  observer: TransactionObserver,
  format: TransactionsFormat,
  source: StateSource,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val transaction by observer(id).collectAsStateWithLifecycle(initialValue = null)
  transaction?.let {
    TransactionItem(it, format, source, onAction, modifier, theme)
  }
}

@Composable
internal fun TransactionItem(
  transaction: Transaction,
  format: TransactionsFormat,
  source: StateSource,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val dimens = LocalTableDimens.current
  Row(
    modifier = modifier
      .fillMaxWidth()
      .wrapContentHeight()
      .background(theme.tableBackground)
      .padding(vertical = dimens.rowVertical, horizontal = dimens.rowHorizontal),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    when (format) {
      TransactionsFormat.List -> TransactionListItem(
        transaction = transaction,
        source = source,
        onAction = onAction,
        theme = theme,
        dimens = dimens,
      )

      TransactionsFormat.Table -> TransactionTableItem(
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
  source: StateSource,
  onAction: ActionListener,
  theme: Theme,
  dimens: TableSpacings,
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
  source: StateSource,
  onAction: ActionListener,
  theme: Theme,
  dimens: TableSpacings,
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

@Composable
private fun String?.orEmptyString() = this ?: Strings.transactionsItemEmpty
