package actual.api.model.account

import actual.account.model.LoginToken
import actual.api.model.Response
import actual.api.model.ResponseStatus
import actual.api.model.internal.LoginResponseDataSerializer
import actual.api.model.internal.LoginResponseSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(LoginResponseSerializer::class)
sealed interface LoginResponse : Response {
  @Serializable
  data class Ok(
    @SerialName("data") val data: Data,
    @SerialName("status") override val status: ResponseStatus = ResponseStatus.Ok,
  ) : LoginResponse

  @Serializable
  data class Error(
    @SerialName("reason") val reason: String,
    @SerialName("status") override val status: ResponseStatus = ResponseStatus.Error,
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
