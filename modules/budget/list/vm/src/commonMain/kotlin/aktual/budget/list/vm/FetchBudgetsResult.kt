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
package aktual.budget.list.vm

import aktual.budget.model.Budget
import androidx.compose.runtime.Immutable

@Immutable
sealed interface FetchBudgetsResult {
  data class Success(
    val budgets: List<Budget>,
  ) : FetchBudgetsResult

  sealed interface Failure : FetchBudgetsResult {
    val reason: String?
  }

  data object NotLoggedIn : Failure {
    override val reason = null
  }

  data class FailureResponse(override val reason: String?) : Failure
  data class InvalidResponse(override val reason: String) : Failure
  data class NetworkFailure(override val reason: String) : Failure
  data class OtherFailure(override val reason: String) : Failure
}
