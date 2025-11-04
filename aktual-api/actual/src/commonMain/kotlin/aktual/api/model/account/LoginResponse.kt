/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
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
