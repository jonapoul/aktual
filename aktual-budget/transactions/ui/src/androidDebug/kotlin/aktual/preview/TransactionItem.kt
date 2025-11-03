/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.budget.model.TransactionsFormat
import aktual.budget.transactions.ui.StateSource
import aktual.budget.transactions.ui.TransactionItem
import aktual.core.ui.PreviewThemedColumn
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
