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
package actual.budget.transactions.ui

import actual.budget.model.TransactionId
import actual.budget.transactions.vm.Transaction
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Immutable
internal fun interface TransactionObserver {
  operator fun invoke(id: TransactionId): Flow<Transaction>
}

internal fun previewObserver(vararg transactions: Transaction) = TransactionObserver { id ->
  val transaction = transactions.firstOrNull { it.id == id } ?: error("No match for $id: $transactions")
  flowOf(transaction)
}
