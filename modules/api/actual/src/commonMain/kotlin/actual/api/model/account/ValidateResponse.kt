package actual.api.model.account

import actual.core.model.LoginMethod
import actual.core.model.PossibleRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ValidateResponse {
  @Serializable
  data class Success(
    @SerialName("data") val data: Data,
  ) : ValidateResponse

  @Serializable
  data class Failure(
    @SerialName("reason") val reason: FailureReason,
  ) : ValidateResponse

  @Serializable
  data class Data(
    @SerialName("userName") val userName: String,
    @SerialName("permission") val permission: PossibleRole,
    @SerialName("userId") val userId: String,
    @SerialName("displayName") val displayName: String,
    @SerialName("loginMethod") val loginMethod: LoginMethod,
  )
}
