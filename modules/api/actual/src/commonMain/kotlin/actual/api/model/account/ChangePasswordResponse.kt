package actual.api.model.account

import dev.drewhamilton.poko.Poko
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

sealed interface ChangePasswordResponse {
  @Serializable
  @Poko class Success(
    @SerialName("data") val data: JsonObject = JsonObject(emptyMap()), // always gives an empty JSON object?
  ) : ChangePasswordResponse

  @Serializable
  @Poko class Failure(
    @SerialName("reason") val reason: FailureReason,
  ) : ChangePasswordResponse
}
