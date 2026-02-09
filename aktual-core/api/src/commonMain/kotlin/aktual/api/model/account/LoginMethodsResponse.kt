package aktual.api.model.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * https://github.com/actualbudget/actual/blob/master/packages/sync-server/src/app-account.js#L58-L61
 */
sealed interface LoginMethodsResponse {
  @Serializable
  data class Success(
      @SerialName("methods") val methods: List<AvailableLoginMethod>,
  ) : LoginMethodsResponse

  @Serializable
  data class Failure(
      @SerialName("reason") val reason: FailureReason,
  ) : LoginMethodsResponse
}
