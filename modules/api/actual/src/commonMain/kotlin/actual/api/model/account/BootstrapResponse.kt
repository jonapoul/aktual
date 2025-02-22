package actual.api.model.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface BootstrapResponse {
  @Serializable
  data class Success(
    @SerialName("data") val data: Data,
  ) : BootstrapResponse

  @Serializable
  data class Failure(
    @SerialName("reason") val reason: String,
  ) : BootstrapResponse

  @Serializable
  data class Data(
    @SerialName("bootstrapped") val bootstrapped: Boolean,
  )
}
