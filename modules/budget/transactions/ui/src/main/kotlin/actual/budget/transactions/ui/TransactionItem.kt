package actual.budget.transactions.ui

import actual.budget.transactions.vm.Transaction
import actual.budget.transactions.vm.TransactionsFormat
import actual.core.ui.LocalTheme
import actual.core.ui.NormalIconButton
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import alakazam.android.ui.compose.HorizontalSpacer
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun TransactionItem(
  transaction: Transaction,
  format: TransactionsFormat,
  checkbox: TransactionCheckbox,
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
        checkbox = checkbox,
        theme = theme,
        dimens = dimens,
      )

      TransactionsFormat.Table -> TransactionTableItem(
        transaction = transaction,
        checkbox = checkbox,
        theme = theme,
        dimens = dimens,
      )
    }
  }
}

@Suppress("unused")
@Composable
private fun RowScope.TransactionListItem(
  transaction: Transaction,
  checkbox: TransactionCheckbox,
  theme: Theme,
  dimens: TableSpacings,
) {
  val isChecked by checkbox.isChecked(transaction.id).collectAsStateWithLifecycle(initialValue = false)
  Checkbox(
    checked = isChecked,
    onCheckedChange = { newValue -> checkbox.onCheckedChange(transaction.id, newValue) },
  )

  HorizontalSpacer(dimens.interColumn)

  Column(
    modifier = Modifier.weight(1f),
  ) {
    Text(
      text = transaction.account,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      textAlign = TextAlign.Start,
      fontSize = dimens.textSize,
    )

    Text(
      text = transaction.payee,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      textAlign = TextAlign.Start,
      fontSize = dimens.textSize,
    )

    Text(
      text = transaction.notes.orEmpty(),
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      textAlign = TextAlign.Start,
      fontSize = dimens.textSize,
    )

    Text(
      text = transaction.category,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      textAlign = TextAlign.Start,
      fontSize = dimens.textSize,
    )
  }

  Text(
    text = transaction.amount.toString(),
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
    textAlign = TextAlign.End,
    fontSize = dimens.textSize,
  )

  HorizontalSpacer(dimens.interColumn)

  NormalIconButton(
    imageVector = Icons.Filled.MoreVert,
    contentDescription = "",
    onClick = {},
  )
}

@Suppress("unused")
@Composable
private fun RowScope.TransactionTableItem(
  transaction: Transaction,
  checkbox: TransactionCheckbox,
  theme: Theme,
  dimens: TableSpacings,
) {
  val isChecked by checkbox.isChecked(transaction.id).collectAsStateWithLifecycle(initialValue = false)
  Checkbox(
    checked = isChecked,
    onCheckedChange = { newValue -> checkbox.onCheckedChange(transaction.id, newValue) },
  )

  HorizontalSpacer(dimens.interColumn)

  Text(
    modifier = Modifier.weight(1f),
    text = transaction.account,
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
    textAlign = TextAlign.Start,
    fontSize = dimens.textSize,
  )

  HorizontalSpacer(dimens.interColumn)

  Text(
    modifier = Modifier.weight(1f),
    text = transaction.payee,
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
    textAlign = TextAlign.Start,
    fontSize = dimens.textSize,
  )

  HorizontalSpacer(dimens.interColumn)

  Text(
    modifier = Modifier.weight(1f),
    text = transaction.notes.orEmpty(),
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
    textAlign = TextAlign.Start,
    fontSize = dimens.textSize,
  )

  HorizontalSpacer(dimens.interColumn)

  Text(
    modifier = Modifier.weight(1f),
    text = transaction.category,
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
    textAlign = TextAlign.Start,
    fontSize = dimens.textSize,
  )

  HorizontalSpacer(dimens.interColumn)

  Text(
    modifier = Modifier.weight(1f),
    text = transaction.amount.toString(),
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
    textAlign = TextAlign.End,
    fontSize = dimens.textSize,
  )

  HorizontalSpacer(dimens.interColumn)

  NormalIconButton(
    imageVector = Icons.Filled.MoreVert,
    contentDescription = "",
    onClick = {},
  )
}

@Preview
@Composable
private fun PreviewListItem() = PreviewColumn {
  TransactionItem(
    checkbox = PreviewTransactionCheckbox,
    format = TransactionsFormat.List,
    transaction = TRANSACTION_1,
  )
}

@Preview
@Composable
private fun PreviewTableItem() = PreviewColumn {
  TransactionItem(
    checkbox = PreviewTransactionCheckbox,
    format = TransactionsFormat.Table,
    transaction = TRANSACTION_1,
  )
}
