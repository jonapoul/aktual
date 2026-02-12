package aktual.api.model.account

import aktual.core.model.Empty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

sealed interface ChangePasswordResponse {
  @Serializable
  data class Success(
    @SerialName("data")
    val data: JsonObject = JsonObject.Empty // always gives an empty JSON object?
  ) : ChangePasswordResponse

  @Serializable
  data class Failure(
    @SerialName("reason") val reason: FailureReason = FailureReason.InvalidPassword,
    @SerialName("details") val details: String? = null,
  ) : ChangePasswordResponse
}
