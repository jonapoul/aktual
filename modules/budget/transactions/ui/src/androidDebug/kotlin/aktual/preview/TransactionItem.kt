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
