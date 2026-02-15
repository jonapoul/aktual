package aktual.api.model.account

import aktual.core.model.LoginMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** https://github.com/actualbudget/actual-server/blob/master/src/app-account.js#L31-L41 */
sealed interface NeedsBootstrapResponse {
  @Serializable data class Success(@SerialName("data") val data: Data) : NeedsBootstrapResponse

  @Serializable
  data class Failure(@SerialName("reason") val reason: FailureReason) : NeedsBootstrapResponse

  @Serializable
  data class Data(
    @SerialName("bootstrapped") val bootstrapped: Boolean,
    @SerialName("loginMethod") val loginMethod: LoginMethod,
    @SerialName("availableLoginMethods") val availableLoginMethods: List<AvailableLoginMethod>,
    @SerialName("multiuser") val isMultiUser: Boolean = false,
  )
}
