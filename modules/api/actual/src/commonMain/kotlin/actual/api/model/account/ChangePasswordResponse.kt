package actual.api.model.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

sealed interface ChangePasswordResponse {
  @Serializable
  data class Success(
    @SerialName("data") val data: JsonObject = JsonObject(emptyMap()), // always gives an empty JSON object?
  ) : ChangePasswordResponse

  @Serializable
  data class Failure(
    @SerialName("reason") val reason: FailureReason,
  ) : ChangePasswordResponse
}
