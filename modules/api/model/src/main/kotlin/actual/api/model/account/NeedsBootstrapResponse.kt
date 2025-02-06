package actual.api.model.account

import actual.api.model.Response
import actual.api.model.ResponseStatus
import actual.api.model.internal.NeedsBootstrapResponseSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * https://github.com/actualbudget/actual-server/blob/master/src/app-account.js#L31-L41
 */
@Serializable(NeedsBootstrapResponseSerializer::class)
sealed interface NeedsBootstrapResponse : Response {
  @Serializable
  data class Ok(
    @SerialName("data") val data: Data,
    @SerialName("status") override val status: ResponseStatus = ResponseStatus.Ok,
  ) : NeedsBootstrapResponse

  @Serializable
  data class Error(
    @SerialName("reason") val reason: String,
    @SerialName("status") override val status: ResponseStatus = ResponseStatus.Error,
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
