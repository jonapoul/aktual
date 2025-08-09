package actual.preview

import actual.budget.model.SortColumn
import actual.budget.model.SortDirection
import actual.budget.model.TransactionsFormat
import actual.budget.transactions.ui.StateSource
import actual.budget.transactions.ui.Transactions
import actual.budget.transactions.ui.previewObserver
import actual.budget.transactions.vm.DatedTransactions
import actual.budget.transactions.vm.TransactionsSorting
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
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
