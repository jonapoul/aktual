package actual.api.model.sync

import actual.api.model.account.FailureReason
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ListUserFilesResponse {
  @Serializable
  data class Success(
    @SerialName("data") val data: List<UserFile>,
  ) : ListUserFilesResponse

  @Serializable
  data class Failure(
    @SerialName("reason") val reason: FailureReason,
  ) : ListUserFilesResponse
}
