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
package actual.api.model.sync

import actual.api.model.account.FailureReason
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface DeleteUserFileResponse {
  @Serializable
  data class Success(
    @SerialName("status") val status: String,
  ) : DeleteUserFileResponse

  @Serializable
  data class Failure(
    @SerialName("status") val status: String,
    @SerialName("reason") val reason: FailureReason,
    @SerialName("details") val details: String?,
  ) : DeleteUserFileResponse
}
