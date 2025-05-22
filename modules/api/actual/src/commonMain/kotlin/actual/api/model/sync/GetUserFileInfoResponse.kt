package actual.api.model.sync

import actual.api.model.account.FailureReason
import dev.drewhamilton.poko.Poko
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface GetUserFileInfoResponse {
  @Serializable
  @Poko class Success(
    @SerialName("data") val data: UserFile,
  ) : GetUserFileInfoResponse

  @Serializable
  @Poko class Failure(
    @SerialName("reason") val reason: FailureReason,
    @SerialName("details") val details: String?,
  ) : GetUserFileInfoResponse
}
