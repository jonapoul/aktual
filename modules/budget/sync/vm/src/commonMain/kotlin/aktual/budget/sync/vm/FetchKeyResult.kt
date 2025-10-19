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
package aktual.budget.sync.vm

import aktual.api.model.account.FailureReason
import aktual.core.model.Base64String

interface FetchKeyResult {
  data class Success(val key: Base64String) : FetchKeyResult

  sealed interface Failure : FetchKeyResult
  data object NotLoggedIn : Failure
  data class IOFailure(val message: String) : Failure
  data class ResponseFailure(val reason: FailureReason) : Failure
  data object TestFailure : Failure
}
