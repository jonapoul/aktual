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
import kotlinx.datetime.LocalDate

@Immutable
internal sealed interface Action {
  data object NavBack : Action
  data class ExpandGroup(val group: LocalDate, val isExpanded: Boolean) : Action
  data class CheckItem(val id: TransactionId, val isChecked: Boolean) : Action
  data class SetPrivacyMode(val isPrivacyEnabled: Boolean) : Action
}

@Immutable
internal fun interface ActionListener {
  operator fun invoke(action: Action)
}
