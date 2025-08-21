package actual.api.model.account

import actual.core.model.LoginToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface BootstrapResponse {
  @Serializable
  data class Success(
    @SerialName("data") val data: Data,
  ) : BootstrapResponse

  @Serializable
  data class Failure(
    @SerialName("reason") val reason: FailureReason,
  ) : BootstrapResponse

  @Serializable
  data class Data(
    @SerialName("token") val token: LoginToken,
  )
}
