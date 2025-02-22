package actual.api.model.account

import actual.account.model.LoginMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * https://github.com/actualbudget/actual-server/blob/master/src/app-account.js#L31-L41
 */
sealed interface NeedsBootstrapResponse {
  @Serializable
  data class Success(
    @SerialName("data") val data: Data,
  ) : NeedsBootstrapResponse

  @Serializable
  data class Failure(
    @SerialName("reason") val reason: String,
  ) : NeedsBootstrapResponse

  @Serializable
  data class Data(
    @SerialName("bootstrapped") val bootstrapped: Boolean,
    @SerialName("loginMethod") val loginMethod: LoginMethod,
    @SerialName("availableLoginMethods") val availableLoginMethods: List<AvailableLoginMethod>,
    @SerialName("multiuser") val multiuser: Boolean = false,
  )

  @Serializable
  data class AvailableLoginMethod(
    @SerialName("method") val method: LoginMethod,
    @SerialName("displayName") val displayName: String,
    @SerialName("active") val active: Int, // 1 == true, 0 == false
  )
}
