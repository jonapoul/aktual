/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.budget.model.SortColumn
import aktual.budget.model.SortDirection
import aktual.budget.model.TransactionsFormat
import aktual.budget.transactions.ui.StateSource
import aktual.budget.transactions.ui.Transactions
import aktual.budget.transactions.ui.previewObserver
import aktual.budget.transactions.vm.DatedTransactions
import aktual.budget.transactions.vm.TransactionsSorting
import aktual.core.ui.PreviewThemedScreen
import aktual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable
import kotlinx.collections.immutable.persistentListOf

@TripleScreenPreview
@Composable
private fun PreviewTransactionsList() = PreviewThemedScreen {
  Transactions(
    source = StateSource.Empty,
    onAction = {},
    format = TransactionsFormat.List,
    sorting = TransactionsSorting(SortColumn.Date, SortDirection.Ascending),
    observer = previewObserver(TRANSACTION_1, TRANSACTION_2, TRANSACTION_3),
    transactions = persistentListOf(
      DatedTransactions(TRANSACTION_1.date, persistentListOf(TRANSACTION_1.id, TRANSACTION_2.id)),
      DatedTransactions(TRANSACTION_3.date, persistentListOf(TRANSACTION_3.id)),
    ),
  )
}

@TripleScreenPreview
@Composable
private fun PreviewTransactionsTable() = PreviewThemedScreen {
  Transactions(
    source = StateSource.Empty,
    onAction = {},
    format = TransactionsFormat.Table,
    sorting = TransactionsSorting(SortColumn.Date, SortDirection.Ascending),
    observer = previewObserver(TRANSACTION_1, TRANSACTION_2, TRANSACTION_3),
    transactions = persistentListOf(
      DatedTransactions(TRANSACTION_1.date, persistentListOf(TRANSACTION_1.id, TRANSACTION_2.id)),
      DatedTransactions(TRANSACTION_3.date, persistentListOf(TRANSACTION_3.id)),
    ),
  )
}

@TripleScreenPreview
@Composable
private fun PreviewTransactionsTableEmpty() = PreviewThemedScreen {
  Transactions(
    source = StateSource.Empty,
    onAction = {},
    format = TransactionsFormat.Table,
    sorting = TransactionsSorting(SortColumn.Date, SortDirection.Ascending),
    observer = previewObserver(TRANSACTION_1, TRANSACTION_2, TRANSACTION_3),
    transactions = persistentListOf(),
  )
}
