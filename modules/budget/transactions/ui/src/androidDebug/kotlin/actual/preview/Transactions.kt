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
