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
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

@Immutable
internal interface StateSource {
  fun isChecked(id: TransactionId): Flow<Boolean>
  fun isExpanded(date: LocalDate): Flow<Boolean>

  object Empty : StateSource {
    override fun isChecked(id: TransactionId): Flow<Boolean> = flowOf()
    override fun isExpanded(date: LocalDate): Flow<Boolean> = flowOf()
  }
}
