package actual.api.model.sync

import actual.api.model.account.FailureReason
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface GetUserFileInfoResponse {
  @Serializable
  data class Success(
    @SerialName("data") val data: UserFile,
  ) : GetUserFileInfoResponse

  @Serializable
  data class Failure(
    @SerialName("reason") val reason: FailureReason,
    @SerialName("details") val details: String? = null,
  ) : GetUserFileInfoResponse
}
