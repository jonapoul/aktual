package aktual.core.model

import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** https://github.com/actualbudget/actual-server/blob/master/src/config-types.ts#L3 */
@Serializable(with = LoginMethod.FallbackSerializer::class)
enum class LoginMethod {
  @SerialName("password") Password,
  @SerialName("header") Header,
  @SerialName("openid") OpenId,
  @Fallback Unknown,
}
