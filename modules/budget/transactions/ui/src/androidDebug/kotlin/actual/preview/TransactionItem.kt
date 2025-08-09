package actual.preview

import actual.budget.model.TransactionsFormat
import actual.budget.transactions.ui.StateSource
import actual.budget.transactions.ui.TransactionItem
import actual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewListItem() = PreviewThemedColumn {
  TransactionItem(
    transaction = TRANSACTION_1,
    format = TransactionsFormat.List,
    source = StateSource.Empty,
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewTableItem() = PreviewThemedColumn {
  TransactionItem(
    transaction = TRANSACTION_1,
    format = TransactionsFormat.Table,
    source = StateSource.Empty,
    onAction = {},
  )
}
