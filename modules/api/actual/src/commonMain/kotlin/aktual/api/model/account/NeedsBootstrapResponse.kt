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
package aktual.api.model.account

import aktual.core.model.LoginMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * https://github.com/actualbudget/actual-server/blob/master/src/app-account.js#L31-L41
 */
sealed interface NeedsBootstrapResponse {
  @Serializable
  data class Success(
    @SerialName("data") val data: Data,
  ) : NeedsBootstrapResponse

  @Serializable
  data class Failure(
    @SerialName("reason") val reason: FailureReason,
  ) : NeedsBootstrapResponse

  @Serializable
  data class Data(
    @SerialName("bootstrapped") val bootstrapped: Boolean,
    @SerialName("loginMethod") val loginMethod: LoginMethod,
    @SerialName("availableLoginMethods") val availableLoginMethods: List<AvailableLoginMethod>,
    @SerialName("multiuser") val multiuser: Boolean = false,
  )
}
