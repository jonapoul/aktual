package actual.api.model.account

import dev.drewhamilton.poko.Poko
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface BootstrapResponse {
  @Serializable
  @Poko class Success(
    @SerialName("data") val data: Data,
  ) : BootstrapResponse

  @Serializable
  @Poko class Failure(
    @SerialName("reason") val reason: FailureReason,
  ) : BootstrapResponse

  @Serializable
  @Poko class Data(
    @SerialName("bootstrapped") val bootstrapped: Boolean,
  )
}
