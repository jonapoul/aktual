package aktual.api.model.account

import aktual.api.model.internal.LoginResponseDataSerializer
import aktual.core.model.Token
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
    val token: Token?

    @Serializable
    data class Valid(
      @SerialName("token")
      override val token: Token,
    ) : Data

    @Serializable
    data class Invalid(
      @SerialName("token")
      override val token: Token? = null,
    ) : Data
  }
}
