package actual.api.model.account

import actual.account.model.LoginToken
import actual.api.model.internal.LoginResponseDataSerializer
import dev.drewhamilton.poko.Poko
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface LoginResponse {
  @Serializable
  @Poko class Success(
    @SerialName("data") val data: Data,
  ) : LoginResponse

  @Serializable
  @Poko class Failure(
    @SerialName("reason") val reason: FailureReason,
  ) : LoginResponse

  @Serializable(LoginResponseDataSerializer::class)
  sealed interface Data {
    val token: LoginToken?

    @Serializable
    @Poko class Valid(
      @SerialName("token")
      override val token: LoginToken,
    ) : Data

    @Serializable
    @Poko class Invalid(
      @SerialName("token")
      override val token: LoginToken? = null,
    ) : Data
  }
}
