package actual.api.model.account

import actual.api.model.Response
import actual.api.model.ResponseStatus
import actual.api.model.internal.ChangePasswordResponseSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable(ChangePasswordResponseSerializer::class)
sealed interface ChangePasswordResponse : Response {
  @Serializable
  data class Ok(
    @SerialName("data") val data: JsonObject = JsonObject(emptyMap()), // doesn't give any response
    @SerialName("status") override val status: ResponseStatus = ResponseStatus.Ok,
  ) : ChangePasswordResponse

  @Serializable
  data class Error(
    @SerialName("reason") val reason: String,
    @SerialName("status") override val status: ResponseStatus = ResponseStatus.Error,
  ) : ChangePasswordResponse
}
