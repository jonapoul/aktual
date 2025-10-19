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

import aktual.api.model.internal.LoginResponseDataSerializer
import aktual.core.model.LoginToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface LoginResponse {
  @Serializable
  data class Success(
    @SerialName("data") val data: Data,
  ) : LoginResponse

  @Serializable
  data class Failure(
    @SerialName("reason") val reason: FailureReason,
  ) : LoginResponse

  @Serializable(LoginResponseDataSerializer::class)
  sealed interface Data {
    val token: LoginToken?

    @Serializable
    data class Valid(
      @SerialName("token")
      override val token: LoginToken,
    ) : Data

    @Serializable
    data class Invalid(
      @SerialName("token")
      override val token: LoginToken? = null,
    ) : Data
  }
}
