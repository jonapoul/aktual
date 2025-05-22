package actual.api.model.sync

import actual.api.model.account.FailureReason
import dev.drewhamilton.poko.Poko
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ListUserFilesResponse {
  @Serializable
  @Poko class Success(
    @SerialName("data") val data: List<UserFile>,
  ) : ListUserFilesResponse

  @Serializable
  @Poko class Failure(
    @SerialName("reason") val reason: FailureReason,
  ) : ListUserFilesResponse {
    constructor(reason: String) : this(FailureReason.Other(reason))
  }
}
