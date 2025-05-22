package actual.api.model.account

import actual.account.model.LoginMethod
import dev.drewhamilton.poko.Poko
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * https://github.com/actualbudget/actual-server/blob/master/src/app-account.js#L31-L41
 */
sealed interface NeedsBootstrapResponse {
  @Serializable
  @Poko class Success(
    @SerialName("data") val data: Data,
  ) : NeedsBootstrapResponse

  @Serializable
  @Poko class Failure(
    @SerialName("reason") val reason: FailureReason,
  ) : NeedsBootstrapResponse

  @Serializable
  @Poko class Data(
    @SerialName("bootstrapped") val bootstrapped: Boolean,
    @SerialName("loginMethod") val loginMethod: LoginMethod,
    @SerialName("availableLoginMethods") val availableLoginMethods: List<AvailableLoginMethod>,
    @SerialName("multiuser") val multiuser: Boolean = false,
  )

  @Serializable
  @Poko class AvailableLoginMethod(
    @SerialName("method") val method: LoginMethod,
    @SerialName("displayName") val displayName: String,
    @SerialName("active") val active: Int, // 1 == true, 0 == false
  )
}
