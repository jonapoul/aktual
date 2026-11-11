package aktual.api.model.account

import aktual.core.model.LoginMethod
import aktual.core.model.PossibleRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ValidateResponse {
  @Serializable data class Success(@SerialName("data") val data: Data) : ValidateResponse

  @Serializable
  data class Failure(@SerialName("reason") val reason: FailureReason) : ValidateResponse

  @Serializable
  data class Data(
    @SerialName("loginMethod") val loginMethod: LoginMethod,
    @SerialName("userName") val userName: String? = null,
    @SerialName("permission") val permission: PossibleRole? = null,
    @SerialName("userId") val userId: String? = null,
    @SerialName("displayName") val displayName: String? = null,
  )
}
