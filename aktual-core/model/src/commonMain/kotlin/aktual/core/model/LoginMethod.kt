package aktual.core.model

import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * https://github.com/actualbudget/actual/blob/master/packages/sync-server/src/load-config.js#L139-L144
 */
@Serializable
enum class LoginMethod {
  @SerialName("password") Password,
  @SerialName("header") Header,
  @SerialName("openid") OpenId,
  @SerialName("api-key") ApiKey,
  @Fallback Unknown,
}
