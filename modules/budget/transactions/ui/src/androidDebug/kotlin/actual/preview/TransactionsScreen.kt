package actual.preview

import actual.budget.model.SortColumn
import actual.budget.model.SortDirection
import actual.budget.model.TransactionsFormat
import actual.budget.transactions.ui.StateSource
import actual.budget.transactions.ui.TransactionsScaffold
import actual.budget.transactions.ui.TransactionsTitleBar
import actual.budget.transactions.ui.previewObserver
import actual.budget.transactions.vm.LoadedAccount
import actual.budget.transactions.vm.TransactionsSorting
import actual.core.ui.PreviewThemedColumn
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.persistentListOf

@Preview
@Composable
private fun TitleBarAll() = PreviewThemedColumn {
  TransactionsTitleBar(
    loadedAccount = LoadedAccount.AllAccounts,
    onAction = {},
  )
}

@Preview
@Composable
private fun TitleBarLoading() = PreviewThemedColumn {
  TransactionsTitleBar(
    loadedAccount = LoadedAccount.Loading,
    onAction = {},
  )
}

@Preview
@Composable
private fun TitleBarSpecific() = PreviewThemedColumn {
  TransactionsTitleBar(
    loadedAccount = LoadedAccount.SpecificAccount(PREVIEW_ACCOUNT),
    onAction = {},
  )
}

@TripleScreenPreview
@Composable
private fun EmptyTable() = PreviewThemedScreen {
  TransactionsScaffold(
    loadedAccount = LoadedAccount.AllAccounts,
    observer = previewObserver(TRANSACTION_1, TRANSACTION_2, TRANSACTION_3),
    format = TransactionsFormat.Table,
    sorting = TransactionsSorting(SortColumn.Date, SortDirection.Descending),
    source = StateSource.Empty,
    transactions = persistentListOf(),
    onAction = {},
  )
}
