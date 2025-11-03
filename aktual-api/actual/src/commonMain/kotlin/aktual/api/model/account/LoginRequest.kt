/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.api.model.account

import aktual.core.model.LoginMethod
import dev.zacsweers.redacted.annotations.Redacted
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import aktual.core.model.Password as PasswordModel

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
