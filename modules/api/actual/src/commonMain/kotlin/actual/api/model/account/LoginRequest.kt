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
package actual.api.model.account

import actual.core.model.LoginMethod
import dev.zacsweers.redacted.annotations.Redacted
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import actual.core.model.Password as PasswordModel

sealed interface LoginRequest {
  @Serializable
  data class Header(
    @SerialName("loginMethod") val loginMethod: LoginMethod = LoginMethod.Header,
  ) : LoginRequest

  @Serializable
  data class OpenId(
    @Redacted @SerialName("password") val password: PasswordModel,
    @SerialName("returnUrl") val returnUrl: String,
    @SerialName("loginMethod") val loginMethod: LoginMethod = LoginMethod.OpenId,
  ) : LoginRequest

  @Serializable
  data class Password(
    @Redacted @SerialName("password") val password: PasswordModel,
    @SerialName("loginMethod") val loginMethod: LoginMethod = LoginMethod.Password,
  ) : LoginRequest
}
