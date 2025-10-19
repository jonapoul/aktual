/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.preview

import aktual.budget.model.SortColumn
import aktual.budget.model.SortDirection
import aktual.budget.model.TransactionsFormat
import aktual.budget.transactions.ui.StateSource
import aktual.budget.transactions.ui.TransactionsScaffold
import aktual.budget.transactions.ui.TransactionsTitleBar
import aktual.budget.transactions.ui.previewObserver
import aktual.budget.transactions.vm.LoadedAccount
import aktual.budget.transactions.vm.TransactionsSorting
import aktual.core.ui.PreviewThemedColumn
import aktual.core.ui.PreviewThemedScreen
import aktual.core.ui.TripleScreenPreview
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
